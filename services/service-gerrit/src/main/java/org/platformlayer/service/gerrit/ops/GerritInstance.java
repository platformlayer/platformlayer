package org.platformlayer.service.gerrit.ops;

import java.io.File;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.ops.JettyInstance;

public class GerritInstance extends OpsTreeBase {

	@Bound
	GerritTemplate template;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(TemplatedFile.build(template, new File(template.getInstanceDir(), "realm.properties")));

		JettyInstance jetty = addChild(JettyInstance.class);
		jetty.template = template;

		GerritWarInstance app = injected(GerritWarInstance.class);
		jetty.addApp(app);
	}

}
