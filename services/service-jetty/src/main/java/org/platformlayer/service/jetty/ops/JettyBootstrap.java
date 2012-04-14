package org.platformlayer.service.jetty.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.tree.OpsTreeBase;

public class JettyBootstrap extends OpsTreeBase {
	@Handler
	public void handler() throws OpsException, IOException {
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		if (OpsContext.isConfigure()) {
			File oldWebappsDir = new File("/var/lib/jetty/webapps.default");
			if (target.getFilesystemInfoFile(oldWebappsDir) == null) {
				File webappsDir = new File("/var/lib/jetty/webapps");
				target.mv(webappsDir, oldWebappsDir);
				target.mkdir(webappsDir, "755");
				target.chown(webappsDir, "jetty", "adm", false, false);
			}

			File oldContextsDir = new File("/etc/jetty/contexts.default");
			if (target.getFilesystemInfoFile(oldContextsDir) == null) {
				File contextsDir = new File("/etc/jetty/contexts");
				target.mv(contextsDir, oldContextsDir);
				target.mkdir(contextsDir, "755");
				target.chown(contextsDir, "root", "root", false, false);
			}
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		File warsDir = new File("/var/lib/jetty/wars");
		addChild(ManagedDirectory.build(warsDir, "755").setOwner("jetty").setGroup("adm"));
	}
}
