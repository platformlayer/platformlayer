package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.CloudImage;
import org.platformlayer.ops.images.ImageFormat;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.instances.ImageFactory;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectCloud;

import com.fathomdb.TimeSpan;

public class DownloadImage extends OpsTreeBase {
	private static final File IMAGES_DIR = new File("/var/lib/lxc/images");

	public File imageFile;
	public List<ImageFormat> imageFormats;

	@Inject
	PlatformLayerClient platformLayer;

	@Inject
	SshKeys sshKeys;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	public PlatformLayerKey recipeKey;

	@Inject
	ImageFactory imageFactory;

	@Inject
	ProviderHelper providers;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (target.getFilesystemInfoFile(imageFile) == null) {
			DirectCloud cloud = OpsContext.get().getInstance(DirectCloud.class);
			if (cloud == null) {
				throw new IllegalStateException("Cloud instance not found");
			}
			ImageStore imageStore = cloudHelpers.getImageStore(cloud);
			MachineProvider machineProvider = providers.toInterface(cloud, MachineProvider.class);
			CloudImage imageInfo = imageFactory.getOrCreateImageId(machineProvider, imageFormats, recipeKey);

			if (imageStore == null) {
				throw new OpsException("Image store not configured");
			}

			String fileName = imageInfo.getId() + ".image." + imageInfo.getFormat().name();

			// TODO: We don't need rawImage; delete or just request a read-only version
			File rawImage = new File(IMAGES_DIR, fileName);
			target.mkdir(IMAGES_DIR);
			// TODO: Caching / reuse ... need to check md5 though
			imageStore.bringToMachine(imageInfo.getId(), target, rawImage);

			ImageFormat imageFormat = imageInfo.getFormat();
			switch (imageFormat) {
			case Tar: {
				target.mkdir(imageFile);
				target.executeCommand(Command.build("cd {0}; tar jxf {1}", imageFile, rawImage).setTimeout(
						TimeSpan.FIVE_MINUTES));
				break;
			}

			case DiskRaw: {
				Command expand = Command.build("gunzip -c {0} | cp --sparse=always /proc/self/fd/0 {1}", rawImage,
						imageFile);
				target.executeCommand(expand.setTimeout(TimeSpan.FIVE_MINUTES));
				break;
			}

			case DiskQcow2: {
				Command expand = Command.build("cp {0} {1}", rawImage, imageFile);
				target.executeCommand(expand.setTimeout(TimeSpan.FIVE_MINUTES));
				break;
			}

			default:
				throw new OpsException("Unknown image format: " + imageFormat);
			}
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(ManagedDirectory.build(IMAGES_DIR, "700"));
	}

}
