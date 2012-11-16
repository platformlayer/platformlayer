package org.platformlayer.service.gerrit.ops;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;

public class GerritWarInstance extends OpsTreeBase {
	@Bound
	GerritTemplate template;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		File jettyBase = template.getJettyInstanceDir();
		// File warsBase = new File(jettyBase, "wars");
		File contextDir = new File(jettyBase, "contexts");
		File webapps = new File(jettyBase, "webapps");

		GerritInstall.addLibExt(this, template);

		File installed = template.getInstallWarFile();
		addChild(ManagedSymlink.build(new File(webapps, "root.war"), installed));

		addChild(TemplatedFile.build(template, new File(contextDir, "gerrit.xml")));
	}
}
