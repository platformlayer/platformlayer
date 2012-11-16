package org.platformlayer.service.gerrit.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.tree.OpsTreeBase;

public class GerritBootstrap extends OpsTreeBase {
	// @Inject
	// TemplateHelpers templates;

	@Handler
	public void handler(OpsTarget target) throws OpsException, IOException {
		GerritInstanceModel template = injected(GerritInstanceModel.class);

		File canary = new File(template.getDataDir(), "bin/gerrit.sh");
		if (target.getFilesystemInfoFile(canary) == null) {
			if (OpsContext.isConfigure()) {
				File dataDir = template.getDataDir();

				Command command = Command.build("java");
				command.addLiteral("-jar").addFile(template.getWarFile());
				command.addLiteral("init");
				command.addLiteral("--no-auto-start");
				command.addLiteral("--batch");
				command.addLiteral("-d").addFile(dataDir);

				target.executeCommand(command);
			}
		}

		// // Nexus needs a workdir; by default it's in the home directory of the user we're running under
		// // With jetty, the jetty user can't create this directory; we do it
		// File sonatypeDir = new File("/usr/share/jetty/sonatype-work");
		// target.mkdir(sonatypeDir, "750");
		//
		// File nexusDir = new File(sonatypeDir, "nexus");
		// target.mkdir(nexusDir, "750");
		//
		// File confDir = new File(nexusDir, "conf");
		// target.mkdir(confDir, "750");
		// {
		// String contents = ResourceUtils.get(getClass(), "conf/security.xml");
		// FileUpload.upload(target, new File(confDir, "security.xml"), contents);
		// }
		//
		// {
		// String contents = ResourceUtils.get(getClass(), "conf/security-configuration.xml");
		// FileUpload.upload(target, new File(confDir, "security-configuration.xml"), contents);
		// }
		//
		//
		// target.chown(sonatypeDir, "jetty", "jetty", true, false);
	}

	@Override
	protected void addChildren() throws OpsException {
		// GerritInstanceModel template = injected(GerritInstanceModel.class);
		//
		// File dataDir = template.getDataDir();
		// addChild(ManagedDirectory.build(dataDir, "700").setOwner(template.getUser()));
	}

}