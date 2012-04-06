package org.platformlayer.service.git.ops;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class GitRepoInit {
    File repoDir;

    @Handler
    public void handler(OpsTarget target) throws OpsException {
        File canaryFile = new File(repoDir, "config");

        if (OpsContext.isConfigure()) {
            if (target.getFilesystemInfoFile(canaryFile) == null) {
                target.executeCommand(Command.build("git --bare init {0}", repoDir));

                File hooks = new File(repoDir, "hooks");
                File postUpdateHook = new File(hooks, "post-update");
                target.mv(new File(hooks, "post-update.sample"), postUpdateHook);
                target.chmod(postUpdateHook, "755");

                target.executeCommand(Command.build("cd {0}; git update-server-info", repoDir));
                target.executeCommand(Command.build("cd {0}; git config http.receivepack true", repoDir));

                target.chown(repoDir, "www-data", "www-data", true, false);
            }
        }
    }
}
