package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.crypto.OpenSshUtils;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.networks.AddressModel;
import org.platformlayer.ops.networks.InterfaceModel;
import org.platformlayer.ops.pool.NetworkAddressPoolAssignment;
import org.platformlayer.ops.pool.SocketAddressPoolAssignment;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.tagger.Tagger;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.ops.CloudInstanceMapper;
import org.platformlayer.service.cloud.direct.ops.DirectCloudUtils;
import org.platformlayer.service.cloud.direct.ops.DownloadImage;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmDrive;
import org.platformlayer.service.cloud.direct.ops.kvm.monitor.KvmConfig.KvmNic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Provider;

public class KvmInstance extends OpsTreeBase {
	public File instanceDir;
	public String id;
	public int minimumMemoryMB;
	public PlatformLayerKey recipeId;
	public PublicKey sshPublicKey;

	// public MachineCreationRequest request;

	private File getInstanceDir() {
		return instanceDir;
	}

	String getEthernetDeviceName() {
		return "tun_" + id + "_0";
	}

	File getImagePath() {
		return new File(getInstanceDir(), "drive0");
	}

	File getConfigIsoPath() {
		return new File(getInstanceDir(), "config.iso");
	}

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		CloudInstanceMapper instance;
		{
			instance = injected(CloudInstanceMapper.class);
			instance.instance = OpsContext.get().getInstance(DirectInstance.class);
			addChild(instance);
		}

		instance.addChild(ManagedDirectory.build(getInstanceDir(), "700"));

		final NetworkAddressPoolAssignment address4;
		{
			address4 = instance.addChild(NetworkAddressPoolAssignment.class);
			address4.holder = getInstanceDir();
			address4.poolProvider = DirectCloudUtils.getPrivateAddressPool4();
		}

		final NetworkAddressPoolAssignment address6;
		{
			address6 = instance.addChild(NetworkAddressPoolAssignment.class);
			address6.holder = getInstanceDir();
			address6.poolProvider = DirectCloudUtils.getAddressPool6();
		}

		{
			NetworkTunDevice tun = injected(NetworkTunDevice.class);
			tun.interfaceName = getEthernetDeviceName();
			tun.bridgeName = new Provider<String>() {
				@Override
				public String get() {
					DirectHost host = OpsContext.get().getInstance(DirectHost.class);
					return host.bridge;
				}
			};
			instance.addChild(tun);
		}

		final SocketAddressPoolAssignment assignMonitorPort;
		{
			assignMonitorPort = injected(SocketAddressPoolAssignment.class);
			assignMonitorPort.holder = getInstanceDir();
			assignMonitorPort.poolProvider = DirectCloudUtils.getKvmMonitorPortPool();
			instance.addChild(assignMonitorPort);
		}

		final SocketAddressPoolAssignment assignVncPort;
		{
			assignVncPort = injected(SocketAddressPoolAssignment.class);
			assignVncPort.holder = getInstanceDir();
			assignVncPort.poolProvider = DirectCloudUtils.getVncPortPool();
			instance.addChild(assignVncPort);
		}

		{
			ConfigIso iso = injected(ConfigIso.class);
			iso.isoFile = getConfigIsoPath();
			iso.buildDir = new File(getInstanceDir(), "config_iso_src");
			iso.model = new TemplateDataSource() {
				@Override
				public void buildTemplateModel(Map<String, Object> model) throws OpsException {
					InterfaceModel eth0 = InterfaceModel.build("eth0");
					AddressModel ipv4 = address4.get();
					eth0.addAddress(ipv4);

					AddressModel ipv6 = address6.get();
					eth0.addAddress(ipv6);

					List<InterfaceModel> interfaces = Lists.newArrayList();
					interfaces.add(eth0);

					model.put("interfaces", interfaces);

					List<String> authorizedKeys = Lists.newArrayList();
					try {
						authorizedKeys.add(OpenSshUtils.serialize(sshPublicKey));
					} catch (IOException e) {
						throw new OpsException("Error serializing ssh key", e);
					}
					model.put("authorizedKeys", authorizedKeys);
				}
			};

			instance.addChild(iso);
		}

		{
			DownloadImage download = injected(DownloadImage.class);
			download.imageFile = getImagePath();
			download.recipeKey = recipeId;
			download.imageFormats = Arrays.asList(ImageFormat.DiskRaw, ImageFormat.DiskQcow2);
			instance.addChild(download);
		}

		{
			ManagedKvmInstance kvmInstance = injected(ManagedKvmInstance.class);

			kvmInstance.id = id;
			kvmInstance.memoryMb = Math.max(256, minimumMemoryMB);
			kvmInstance.vcpus = 1;
			kvmInstance.base = getInstanceDir();
			kvmInstance.monitor = assignMonitorPort;
			kvmInstance.vnc = assignVncPort;
			kvmInstance.nics = buildVnics();
			kvmInstance.drives = buildDrives();

			instance.addChild(kvmInstance);
		}

		{
			final DirectInstance model = OpsContext.get().getInstance(DirectInstance.class);

			OpsProvider<TagChanges> tagChanges = new OpsProvider<TagChanges>() {
				@Override
				public TagChanges get() {
					TagChanges tagChanges = new TagChanges();

					tagChanges.addTags.add(new Tag(Tag.INSTANCE_KEY, model.getKey().getUrl()));

					AddressModel ipv4 = address4.get();
					AddressModel ipv6 = address6.get();

					tagChanges.addTags.add(ipv4.toTag());
					tagChanges.addTags.add(ipv6.toTag());

					return tagChanges;
				}
			};

			addChild(Tagger.build(model, tagChanges));
		}
	}

	protected Map<String, String> asMap(Properties properties) {
		Map<String, String> map = Maps.newHashMap();
		for (Object key : properties.keySet()) {
			map.put((String) key, (String) properties.get(key));
		}
		return map;
	}

	private List<KvmNic> buildVnics() {
		List<KvmNic> nics = Lists.newArrayList();

		{
			KvmNic nic = new KvmNic();
			nic.device = getEthernetDeviceName();
			// nic.mac ;
			// Also verify that the guest's running Kernel has CONFIG_PCI_MSI enabled:
			//
			// grep CONFIG_PCI_MSI /boot/config-`uname -r`
			// If both conditions are met, use the vhost-net driver by starting the guest with the following example
			// command line:
			//
			// qemu-kvm [...] -netdev tap,id=guest0,vhost=on,script=no
			// -net nic,model=virtio,netdev=guest0,macaddr=00:16:35:AF:94:4B
			//
			// qemu: Supported NIC models: ne2k_pci,i82551,i82557b,i82559er,rtl8139,e1000,pcnet,virtio
			nic.model = "virtio"; // "e1000";
			nic.name = "nic0";
			nics.add(nic);
		}

		return nics;
	}

	private Provider<List<KvmDrive>> buildDrives() {
		return new Provider<List<KvmDrive>>() {
			@Override
			public List<KvmDrive> get() {
				List<KvmDrive> drives = Lists.newArrayList();

				{
					KvmDrive drive = new KvmDrive();
					drive.path = getImagePath().getAbsolutePath();
					drive.id = "0";
					drive.boot = true;
					drive.format = "raw";
					drive.media = "disk";

					OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);
					ProcessExecution fileCommand;
					try {
						fileCommand = target.executeCommand(Command.build("file --brief {0}", getImagePath()));
					} catch (OpsException e) {
						throw new IllegalStateException("Error querying file type", e);
					}
					String fileStdout = fileCommand.getStdOut();
					if (fileStdout.contains("QCOW Image")) {
						drive.format = "qcow2";
					}

					drives.add(drive);
				}
				{
					KvmDrive drive = new KvmDrive();

					drive.path = getConfigIsoPath().getAbsolutePath();
					drive.id = "config";
					drive.boot = false;
					drive.format = "raw";
					drive.media = "cdrom";

					drives.add(drive);
				}

				return drives;
			}
		};

	}
}
