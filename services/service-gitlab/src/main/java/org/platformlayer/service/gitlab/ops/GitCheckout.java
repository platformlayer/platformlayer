package org.platformlayer.service.gitlab.ops;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;

public class GitCheckout {

	private static final Logger log = LoggerFactory.getLogger(GitCheckout.class);

	public File targetDir;
	public String source;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			if (target.getFilesystemInfoFile(targetDir) != null) {
				log.warn("Directory already exists; skipping clone (should we update?)");
			} else {
				File checkoutDir = targetDir.getParentFile();
				Command command = Command.build("cd {0}; git clone {1}", checkoutDir, source);
				command.setTimeout(TimeSpan.FIVE_MINUTES);
				target.executeCommand(command);
			}
		}
	}

}
