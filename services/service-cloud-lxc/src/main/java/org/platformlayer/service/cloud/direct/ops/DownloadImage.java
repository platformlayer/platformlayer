package org.platformlayer.service.cloud.direct.ops;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectCloud;

public class DownloadImage extends OpsTreeBase {
    private static final File IMAGES_DIR = new File("/var/lib/lxc/images");

    public File imageFile;
    public ImageFactory.ImageFormat imageFormat;

    @Inject
    PlatformLayerClient platformLayer;

    @Inject
    SshKeys sshKeys;

    @Inject
    PlatformLayerCloudHelpers cloudHelpers;

    public PlatformLayerKey recipeKey;

    @Inject
    ImageFactory imageFactory;

    @Handler
    public void handler(OpsTarget target) throws OpsException {
        if (target.getFilesystemInfoFile(imageFile) == null) {
            DirectCloud cloud = OpsContext.get().getInstance(DirectCloud.class);
            if (cloud == null) {
                throw new IllegalStateException("Cloud instance not found");
            }
            ImageStore imageStore = cloudHelpers.getImageStore(cloud);
            String imageId = imageFactory.getOrCreateImageId(cloud, imageFormat, recipeKey);

            if (imageStore == null) {
                throw new OpsException("Image store not configured");
            }

            // TODO: We don't need rawImage; delete or just request a read-only version
            File rawImage = new File(IMAGES_DIR, imageId + ".image");
            // TODO: Caching / reuse ... need to check md5 though
            imageStore.bringToMachine(imageId, target, rawImage);

            switch (imageFormat) {
            case Tar: {
                target.mkdir(imageFile);
                target.executeCommand(Command.build("cd {0}; tar jxf {1}", imageFile, rawImage).setTimeout(TimeSpan.FIVE_MINUTES));
                break;
            }

            case DiskRaw: {
                Command expand = Command.build("gunzip -c {0} | cp --sparse=always /proc/self/fd/0 {1}", rawImage, imageFile);
                target.executeCommand(expand.setTimeout(TimeSpan.FIVE_MINUTES));
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
