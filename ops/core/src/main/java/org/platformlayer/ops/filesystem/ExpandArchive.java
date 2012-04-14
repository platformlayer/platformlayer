package org.platformlayer.ops.filesystem;

import java.io.File;

import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class ExpandArchive {
	public File zipFile;
	public File extractPath;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			target.mkdir(extractPath);

			String archiveFileName = zipFile.getName();

			if (archiveFileName.endsWith(".zip")) {
				// -u = update, for (something close to) idempotency
				// -o = overwrite (no prompt)
				Command unzipCommand = Command.build("unzip -u -o {0} -d {1}",
						zipFile, extractPath);
				target.executeCommand(unzipCommand
						.setTimeout(TimeSpan.FIVE_MINUTES));
			} else if (archiveFileName.endsWith(".tgz")
					|| archiveFileName.endsWith(".tar.gz")) {
				Command unzipCommand = Command.build("cd {0}; tar zxf {1}",
						extractPath, zipFile);
				target.executeCommand(unzipCommand
						.setTimeout(TimeSpan.FIVE_MINUTES));

			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
}
