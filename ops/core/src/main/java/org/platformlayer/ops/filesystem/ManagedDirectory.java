package org.platformlayer.ops.filesystem;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OperationType;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsTarget;

//@Icon("folder")
public class ManagedDirectory extends ManagedFilesystemItem {

    public static ManagedDirectory build(String path, String mode) {
        return build(new File(path), mode);
    }

    public static ManagedDirectory build(File path, String mode) {
        ManagedDirectory dir = OpsContext.get().getInjector().getInstance(ManagedDirectory.class);
        dir.filePath = path;
        dir.fileMode = mode;
        return dir;
    }

    public ManagedDirectory() {
        fileMode = "0700";
    }

    // static final Logger log = Logger.getLogger(ManagedDirectoryOpsItem.class);
    //
    @Handler
    public void doConfigureValidate(OperationType operationType, OpsTarget target) throws Exception {
        File path = getFilePath();
        FilesystemInfo fsInfo = target.getFilesystemInfoFile(path);
        boolean exists = (fsInfo != null);

        if (operationType.isConfigure()) {
            if (!exists) {
                target.mkdir(path, fileMode);

                // doUpdateAction(operation);
            }

            configureOwnerAndMode(target, fsInfo);
        }

        if (operationType.isValidate()) {
            if (!exists) {
                OpsContext.get().addWarning(this, "DoesNotExist", "Directory not found: " + getFilePath());
                return;
            }

            validateOwner(fsInfo);

            validateMode(fsInfo);
        }

    }

}
