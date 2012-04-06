package org.platformlayer.service.cloud.direct.ops.kvm;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;

public class MkIsoFs {
    public String volumeLabel;
    public File srcDir;
    public File iso;

    @Handler
    public void handler(OpsTarget target) throws OpsException {
        FilesystemInfo isoInfo = target.getFilesystemInfoFile(iso);

        boolean rebuild = true;
        if (isoInfo != null) {
            // TODO: Do timestamp based dependency checking?
            rebuild = false;
        }

        if (rebuild) {
            Command mkisoCommand = Command.build("genisoimage -input-charset utf-8 -R -o {0}", iso);
            if (volumeLabel != null) {
                mkisoCommand.addLiteral("-V").addQuoted(volumeLabel);
            }
            mkisoCommand.addFile(srcDir);

            target.executeCommand(mkisoCommand);
        }
    }
}
