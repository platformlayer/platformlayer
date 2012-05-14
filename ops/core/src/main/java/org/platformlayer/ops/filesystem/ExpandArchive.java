package org.platformlayer.ops.filesystem;

import java.io.File;

import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

public class ExpandArchive extends OpsTreeBase {
	public File archiveFile;
	public File extractPath;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		if (OpsContext.isConfigure()) {
			target.mkdir(extractPath);

			String archiveFileName = archiveFile.getName();

			if (archiveFileName.endsWith(".zip")) {
				// -u = update, for (something close to) idempotency
				// -o = overwrite (no prompt)
				Command unzipCommand = Command.build("unzip -u -o {0} -d {1}", archiveFile, extractPath);
				target.executeCommand(unzipCommand.setTimeout(TimeSpan.FIVE_MINUTES));
			} else if (archiveFileName.endsWith(".tgz") || archiveFileName.endsWith(".tar.gz")) {
				Command unzipCommand = Command.build("cd {0}; tar zxf {1}", extractPath, archiveFile);
				target.executeCommand(unzipCommand.setTimeout(TimeSpan.FIVE_MINUTES));
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
