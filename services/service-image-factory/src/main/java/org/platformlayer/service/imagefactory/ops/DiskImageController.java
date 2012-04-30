package org.platformlayer.service.imagefactory.ops;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ExceptionUtils;
import org.platformlayer.ResourceUtils;
import org.platformlayer.Strings;
import org.platformlayer.TimeSpan;
import org.platformlayer.TimeoutPoll;
import org.platformlayer.TimeoutPoll.PollFunction;
import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.ChrootOpsTarget;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.CommandEnvironment;
import org.platformlayer.ops.EnumUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;
import org.platformlayer.ops.helpers.AptHelper;
import org.platformlayer.ops.helpers.HttpProxyHelper;
import org.platformlayer.ops.helpers.HttpProxyHelper.Usage;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.ImageFactory.ImageFormat;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;
import org.platformlayer.service.imagefactory.OperatingSystem;
import org.platformlayer.service.imagefactory.OperatingSystem.Distribution;
import org.platformlayer.service.imagefactory.model.ConfigurePackage;
import org.platformlayer.service.imagefactory.model.DiskImage;
import org.platformlayer.service.imagefactory.model.DiskImageRecipe;
import org.platformlayer.service.imagefactory.model.OperatingSystemRecipe;
import org.platformlayer.service.imagefactory.model.Repository;
import org.platformlayer.service.imagefactory.model.RepositoryKey;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DiskImageController {
	static final Logger log = Logger.getLogger(DiskImageController.class);

	@Inject
	CloudContext cloud;

	@Inject
	OpsContext opsContext;

	@Inject
	PackageHelpers packageHelpers;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Inject
	ServiceContext service;

	@Inject
	AptHelper apt;

	@Inject
	HttpProxyHelper httpProxies;

	protected OperatingSystem getRequestedOperatingSystem(DiskImageRecipe recipe) {
		OperatingSystemRecipe operatingSystemRecipe = recipe.operatingSystem;
		if (operatingSystemRecipe == null) {
			operatingSystemRecipe = new OperatingSystemRecipe();
		}

		if (Strings.isEmpty(operatingSystemRecipe.distribution)) {
			return OperatingSystem.DebianSqueeze;
		}

		Distribution distribution = Distribution.parse(operatingSystemRecipe.distribution);

		String version = operatingSystemRecipe.version;
		if (Strings.isEmpty(version)) {
			version = distribution.getDefaultOsVersion();
		}

		return new OperatingSystem(distribution, version);
	}

	@Handler
	public void handler(DiskImage image) throws OpsException, IOException {
		// Assume the worst...
		opsContext.setFailure(true);

		MachineCloudBase cloudModel = cloudHelpers.getCloud(image.cloud);
		DiskImageRecipe recipe = platformLayer.getItem(image.recipeId, DiskImageRecipe.class);

		OperatingSystem operatingSystem = getRequestedOperatingSystem(recipe);

		String kernelPackage = packageHelpers.getDefaultKernelPackage(operatingSystem);
		String filesystem = "ext3";

		ImageFormat imageFormat = EnumUtils.valueOfCaseInsensitive(ImageFormat.class, image.format);

		boolean buildTar = imageFormat == ImageFormat.Tar;

		// TODO: This logic is not intrinsically correct
		// boolean supportCloudConfigDisk = imageFormat != ImageFormat.DiskQcow2;
		boolean supportCloudConfigDisk = true;
		boolean useConfigDriveSymlinks = false;
		String configDriveLabel = "config";

		List<String> packages = Lists.newArrayList();
		packages.add("openssh-server");

		// Needed for preseeding
		packages.add("debconf-utils");

		if (operatingSystem.getDistribution() == Distribution.Debian) {
			packages.add("locales");
		}

		// We always want some basics available in our images
		packages.add("curl");

		String hostname = "openstack";

		MachineCreationRequest request = new MachineCreationRequest();

		SshKey sshKey = service.getSshKey();

		// There are problems using LXC with debootstrap
		request.hostPolicy = new HostPolicy();
		request.hostPolicy.allowRunInContainer = false;

		request.recipeId = null; // Null means 'use bootstrap image'
		request.sshPublicKey = sshKey.getKeyPair().getPublic();
		request.sshPublicKeyName = service.getSshKeyName();
		request.securityGroups = Lists.newArrayList();

		String securityGroup = service.getSecurityGroupName();
		request.securityGroups.add(securityGroup);

		// We don't need a lot of memory to build a disk image (I think!)
		request.minimumMemoryMB = 256;

		Machine machine = cloud.createInstance(request, OpsSystem.toKey(image));

		opsContext.takeOwnership(machine);

		machine = waitForAddress(machine);
		OpsTarget target = machine.getTarget(sshKey);
		waitForTarget(target);

		File tempDir = target.createTempDir();

		apt.update(target);

		// We need to install curl first so we can detect the performance of our proxies
		apt.install(target, "curl"); // Needed for proxy testing at least

		CommandEnvironment httpProxyEnv = httpProxies.getHttpProxyEnvironment(target, Usage.SoftwarePackages);

		// For now, we assume that this image doesn't have debootstrap pre-installed
		apt.install(target, "debootstrap");

		// For transferring the file to a direct image server
		apt.install(target, "socat");

		// debootstrap with LXC seems to have serious problems...
		boolean supportLxc = false;

		if (supportLxc) {
			apt.install(target, "fakechroot", "fakeroot");
		}

		Command command;

		File rootfsDir;
		File imageFile;
		File loopbackPartition = null;

		if (!buildTar) {
			apt.install(target, "mbr");
			apt.install(target, "parted");
			apt.install(target, "kpartx");
			apt.install(target, "extlinux");

			// Same with qemu-kvm
			// (needed for qemu-img convert ... a lot of extra stuff for just the
			// utils!)
			String qemuImgPackage = packageHelpers.getPackageFor("qemu-img", operatingSystem);
			apt.install(target, qemuImgPackage);

			// Use local ephemeral storage...
			imageFile = new File(tempDir, "image.raw");
			command = Command.build("dd if=/dev/null bs=1M seek=8180 of={0}", imageFile);
			target.executeCommand(command);

			// Create partitions
			target.executeCommand(Command.build("parted -s {0} mklabel msdos", imageFile));
			target.executeCommand(Command.build("parted -s {0} mkpart primary 0% 100%", imageFile));
			target.executeCommand(Command.build("parted -s {0} set 1 boot on", imageFile));

			// Install Master Boot Record
			target.executeCommand(Command.build("install-mbr {0}", imageFile));

			// Mount the partitions
			// Hopefully it’s loop0p1...
			target.executeCommand(Command.build("modprobe dm-mod"));
			target.executeCommand(Command.build("kpartx -av {0}", imageFile));

			loopbackPartition = new File("/dev/mapper/loop0p1");

			// Format filesystem
			command = Command.build("yes | mkfs." + filesystem + " {0}", loopbackPartition);
			command.setTimeout(TimeSpan.FIVE_MINUTES);
			target.executeCommand(command);

			// Get this onto disk now, so we don't delay later commands
			target.executeCommand(Command.build("sync").setTimeout(TimeSpan.FIVE_MINUTES));

			// Don’t force a check based on dates
			target.executeCommand(Command.build("tune2fs -i 0 {0}", loopbackPartition)
					.setTimeout(TimeSpan.FIVE_MINUTES));

			// Get this onto disk now, so we don't delay later commands
			target.executeCommand(Command.build("sync").setTimeout(TimeSpan.FIVE_MINUTES));

			// Mount on mnt/
			File mntDir = new File(tempDir, "mnt");
			target.executeCommand("mkdir {0}", mntDir);

			target.executeCommand(Command.build("mount {0} {1}", loopbackPartition, mntDir).setTimeout(
					TimeSpan.FIVE_MINUTES));

			rootfsDir = mntDir;
		} else {
			rootfsDir = new File(tempDir, "rootfs");
			imageFile = new File(tempDir, "image.tar.bz2");
		}

		if (buildTar) {
			apt.install(target, "bzip2");
		}

		// Do debootstrap

		if (supportLxc) {
			command = Command.build("fakechroot fakeroot debootstrap");
		} else {
			command = Command.build("debootstrap");
		}

		command.addLiteral("--verbose");
		command.addLiteral("--resolve-deps");
		if (supportLxc) {
			// Lxc has problems with mounting etc; fakechroot avoids this
			command.addLiteral("--variant=fakechroot");
			// command.addLiteral("--variant=minbase");
		}
		command.addQuoted("--include=", Joiner.on(",").join(packages));
		command.addLiteral(operatingSystem.getVersion());
		command.addFile(rootfsDir);
		// command.addQuoted(aptSource);

		command.setEnvironment(httpProxyEnv);

		command.setTimeout(TimeSpan.THIRTY_MINUTES);

		try {
			target.executeCommand(command);
		} catch (ProcessExecutionException e) {
			String debootstrapLog = target.readTextFile(new File(rootfsDir, "debootstrap/debootstrap.log"));
			log.warn("Debootstrap log: " + debootstrapLog);

			throw new OpsException("Error running debootstrap", e);
		}

		// TODO: Switch to ChrootOpsTarget, so we can move this stuff into utility functions
		ChrootOpsTarget chrootTarget = new ChrootOpsTarget(rootfsDir, new File("/tmp"), target);

		target.setFileContents(new File(rootfsDir, "etc/hostname"), hostname);

		{
			// Stop services being started in the chroot
			String policy = ResourceUtils.get(getClass(), "usr.sbin.policy-rc.d");
			File policyFile = new File(rootfsDir, "usr/sbin/policy-rc.d");
			target.setFileContents(policyFile, policy);
			target.chmod(policyFile, "755");
		}

		target.executeCommand("mount -t proc proc {0}", new File(rootfsDir, "proc"));

		target.executeCommand(Command.build("chroot {0} apt-get --yes update", rootfsDir).setEnvironment(httpProxyEnv)
				.setTimeout(TimeSpan.FIVE_MINUTES));
		target.executeCommand("chroot {0} locale-gen en_US.utf8", rootfsDir);

		target.executeCommand("chroot {0} /bin/bash -c \"DEBIAN_FRONTEND=noninteractive dpkg-reconfigure locales\"",
				rootfsDir);

		if (!buildTar) {
			{
				File kernelImgConf = new File(rootfsDir, "etc/kernel-img.conf");

				String preseedData = ResourceUtils.get(getClass(), "kernel-img.conf");
				target.setFileContents(kernelImgConf, preseedData);
			}

			{
				File preseedTmpDir = target.createTempDir();
				File preseedFile = new File(preseedTmpDir, "kernel.preseed");

				String preseedData = ResourceUtils.get(getClass(), "kernel.preseed");
				target.setFileContents(preseedFile, preseedData);

				target.executeCommand(Command.build("cat {0} | chroot {1} debconf-set-selections", preseedFile,
						rootfsDir));
				target.executeCommand(Command.build("chroot {0} apt-get --yes install {1}", rootfsDir, kernelPackage)
						.setEnvironment(httpProxyEnv).setTimeout(TimeSpan.FIFTEEN_MINUTES));
			}
		}

		preconfigurePackages(chrootTarget, recipe.configurePackage);

		if (recipe.repositoryKey != null) {
			addRepositoryKeys(chrootTarget, recipe.repositoryKey);
		}

		if (recipe.repository != null) {
			addRepositories(chrootTarget, recipe.repository);

			apt.update(chrootTarget);
		}

		if (recipe.addPackage != null) {
			for (String packageName : recipe.addPackage) {
				target.executeCommand(Command.build("chroot {0} apt-get --yes install {1}", rootfsDir, packageName)
						.setEnvironment(httpProxyEnv).setTimeout(TimeSpan.FIFTEEN_MINUTES));

				if (packageName.equals("jenkins")) {
					// It looks like jenkins doesn't honor policy-rc.d (?)
					// TODO: Fix this monstrosity...
					log.warn("Hard-coding service stop after jenkins installation");
					target.executeCommand(Command.build("chroot {0} /etc/init.d/jenkins stop", rootfsDir));
				}
			}
		}

		target.executeCommand(Command.build("chroot {0} apt-get --yes upgrade", rootfsDir).setEnvironment(httpProxyEnv)
				.setTimeout(TimeSpan.FIVE_MINUTES));
		target.executeCommand(Command.build("chroot {0} apt-get clean", rootfsDir).setEnvironment(httpProxyEnv));

		if (!buildTar) {
			String uuid;
			{
				ProcessExecution uuidExecution = target.executeCommand("blkid -o value -s UUID {0}", loopbackPartition);
				uuid = uuidExecution.getStdOut().trim();
			}

			// Set up /etc/fstab
			String fstab = "# /etc/fstab: static file system information.\n";
			// TODO: Swap
			fstab += "proc\t/proc\tproc\tnodev,noexec,nosuid\t0\t0\n";
			// fstab += "/dev/sda1\t/\t" + filesystem +
			// "\terrors=remount-ro\t0\t1\n";
			fstab += String.format("UUID=%s\t/\t%s\terrors=remount-ro\t0\t1\n", uuid, filesystem);

			if (supportCloudConfigDisk) {
				if (useConfigDriveSymlinks) {
					// Use configuration from cloud_config mount
					target.mkdir(new File(rootfsDir, "media/config"));
					fstab += "/dev/disk/by-label/" + configDriveLabel + "\t/media/config\tudf,iso9660\tro\t0\t0\n";
				}
			}

			target.setFileContents(new File(rootfsDir, "etc/fstab"), fstab);
			log.info("fstab = " + fstab);

			// Set up extlinux
			{
				ProcessExecution kernelExecution = target.executeCommand("chroot {0} find boot/ -name \"vmlinuz-*\"",
						rootfsDir);
				List<String> kernels = Lists.newArrayList();
				for (String kernel : kernelExecution.getStdOut().split("\n")) {
					kernel = kernel.trim();
					if (kernel.isEmpty()) {
						continue;
					}
					kernels.add(kernel);
				}

				if (kernels.size() > 1) {
					throw new IllegalStateException("Multiple kernels found");
				} else if (kernels.size() != 1) {
					throw new IllegalStateException("No kernels found");
				}

				ProcessExecution initrdExecution = target.executeCommand("chroot {0} find boot/ -name \"initrd*\"",
						rootfsDir);
				List<String> initrds = Lists.newArrayList();
				for (String initrd : initrdExecution.getStdOut().split("\n")) {
					initrd = initrd.trim();
					if (initrd.isEmpty()) {
						continue;
					}
					if (initrd.endsWith(".bak")) {
						continue;
					}
					initrds.add(initrd);
				}

				if (initrds.size() > 1) {
					throw new IllegalStateException("Multiple initrds found");
				} else if (initrds.size() != 1) {
					throw new IllegalStateException("No initrds found");
				}

				String conf = String.format(
						"default linux\ntimeout 1\n\nlabel linux\nkernel %s\nappend initrd=%s root=UUID=%s ro quiet",
						kernels.get(0), initrds.get(0), uuid);
				target.setFileContents(new File(rootfsDir, "extlinux.conf"), conf);
				log.info("extlinux.conf = " + conf);
			}
			target.executeCommand(Command.build("extlinux --install  {0}", rootfsDir).setTimeout(TimeSpan.FIVE_MINUTES));
		}

		if (supportCloudConfigDisk) {
			if (useConfigDriveSymlinks) {
				target.rm(new File(rootfsDir, "etc/network/interfaces"));
				target.executeCommand("ln -s /media/config/etc/network/interfaces {0}", new File(rootfsDir,
						"etc/network/interfaces"));

				target.mkdir(new File(rootfsDir, "root/.ssh"));
				target.executeCommand("ln -s /media/config/root/.ssh/authorized_keys {0}", new File(rootfsDir,
						"root/.ssh/authorized_keys"));
			} else {
				String initScript = ResourceUtils.get(getClass(), "openstack-config");
				File initScriptFile = new File(rootfsDir, "etc/init.d/openstack-config");

				target.setFileContents(initScriptFile, initScript);
				target.executeCommand("chmod +x {0}", initScriptFile);

				chrootTarget.executeCommand("/usr/sbin/update-rc.d openstack-config defaults");
			}
		}

		{
			// Remove policy file
			File policyFile = new File(rootfsDir, "usr/sbin/policy-rc.d");
			target.rm(policyFile);
		}

		target.executeCommand("sync");
		target.executeCommand("umount {0}", new File(rootfsDir, "proc"));

		if (!buildTar) {
			target.executeCommand("sync");
			target.executeCommand("umount {0}", rootfsDir);
			target.executeCommand("sync");
			target.executeCommand("kpartx -d {0}", imageFile);
			target.executeCommand("sync");
		}

		if (buildTar) {
			Command compress = Command.build("cd {0}; tar jcf {1} .", rootfsDir, imageFile);
			target.executeCommand(compress.setTimeout(TimeSpan.FIFTEEN_MINUTES));
		}

		FilesystemInfo imageInfo = target.getFilesystemInfoFile(imageFile);

		File uploadImageFile;

		if (!buildTar) {
			boolean isQcow2 = imageFormat == ImageFormat.DiskQcow2;

			if (isQcow2) {
				// We create the image as a raw image (making use of sparse files)
				// and then convert it to qcow2. This is a little less efficient, but
				// has a few advantages...
				// 1) We can support different formats
				// 2) The final image is defragmented
				// 3) Mounting a qcow2 image (or other image formats) is tricky vs
				// loopback mount
				uploadImageFile = new File(imageFile.getParentFile(), "image.qcow2");
				command = Command.build("qemu-img convert -f raw -O qcow2 {0} {1}", imageFile, uploadImageFile);
				command.setTimeout(TimeSpan.THIRTY_MINUTES);
				target.executeCommand(command);
			} else {
				uploadImageFile = new File(imageFile.getParentFile(), "image.raw.gz");

				command = Command.build("gzip -c --best {0} > {1}", imageFile, uploadImageFile);
				command.setTimeout(TimeSpan.THIRTY_MINUTES);
				target.executeCommand(command);
			}
		} else {
			uploadImageFile = imageFile;
		}

		String imageId;

		// Upload & tag the image with the recipe ID
		{
			Tags tags = new Tags();
			tags.add(opsContext.getOpsSystem().createParentTag(recipe));
			tags.add(ImageFactory.buildImageFormatTag(imageFormat));

			imageId = cloud.getImageStore(cloudModel).uploadImage(target, tags, uploadImageFile, imageInfo.size);
		}

		// Tag the recipe with the image ID
		{
			TagChanges tagChanges = new TagChanges();
			tagChanges.addTags.add(new Tag(Tag.IMAGE_ID, imageId));
			platformLayer.changeTags(OpsSystem.toKey(image), tagChanges);
		}

		// Our pessimism proved unfounded...
		opsContext.setFailure(false);
	}

	private void addRepositoryKeys(OpsTarget target, List<RepositoryKey> keys) throws OpsException {
		for (RepositoryKey key : keys) {
			apt.addRepositoryKeyUrl(target, key.url);
		}
	}

	private void addRepositories(OpsTarget target, List<Repository> repositories) throws OpsException {
		for (Repository repository : repositories) {
			apt.addRepository(target, repository.key, repository.source);
		}
	}

	private void preconfigurePackages(OpsTarget target, List<ConfigurePackage> configurePackages) throws OpsException,
			IOException {
		if (configurePackages == null || configurePackages.isEmpty()) {
			return;
		}

		List<org.platformlayer.service.imagefactory.v1.ConfigurePackage> list = Lists.newArrayList();
		for (ConfigurePackage configurePackage : configurePackages) {
			org.platformlayer.service.imagefactory.v1.ConfigurePackage model = new org.platformlayer.service.imagefactory.v1.ConfigurePackage();
			model.setPackageName(configurePackage.packageName);
			model.setType(configurePackage.type);
			model.setKey(configurePackage.key);
			model.setValue(configurePackage.value);
			list.add(model);
		}

		apt.preconfigurePackages(target, list);
	}

	private String waitForTarget(final OpsTarget target) throws OpsException {
		// I think there's a nasty race condition here, where we try to log in too early, and this somehow screws up the
		// key
		// Maybe the config CD isn't yet mounted? Maybe SSH isn't yet started? Not sure yet..
		try {
			log.info("Sleeping in the hope of avoiding (not fully understood) SSH issue");
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			ExceptionUtils.handleInterrupted(e);
			throw new OpsException("Interrupted", e);
		}

		try {
			return TimeoutPoll.poll(TimeSpan.FIVE_MINUTES, TimeSpan.TEN_SECONDS, new PollFunction<String>() {
				@Override
				public String call() throws Exception {
					try {
						ProcessExecution execution = target.executeCommand("uname -srp");
						return execution.getStdOut();
					} catch (ProcessExecutionException e) {
						log.info("Waiting for machine; got process execution error", e);
						return null;
					}
				}
			});
		} catch (ExecutionException e) {
			throw new OpsException("Error while waiting for machine", e);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout while waiting for machine", e);
		}
	}

	private Machine waitForAddress(final Machine machine) throws OpsException {
		try {
			final NetworkPoint myNetworkPoint = NetworkPoint.forMe();

			String address = machine.findAddress(myNetworkPoint, 22);
			if (address != null) {
				return machine;
			}

			return TimeoutPoll.poll(TimeSpan.FIVE_MINUTES, TimeSpan.TEN_SECONDS, new PollFunction<Machine>() {
				@Override
				public Machine call() throws Exception {
					Machine refreshed = cloud.refreshMachine(machine);

					String address = refreshed.findAddress(myNetworkPoint, 22);
					if (address != null) {
						return refreshed;
					}
					return null;
				}
			});
		} catch (ExecutionException e) {
			throw new OpsException("Error while waiting for address", e);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout while waiting for address", e);
		}
	}
}
