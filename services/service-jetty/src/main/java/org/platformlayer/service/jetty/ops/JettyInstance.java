package org.platformlayer.service.jetty.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;

public class JettyInstance extends OpsTreeBase {
	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		JettyTemplate template = injected(JettyTemplate.class);
		// We've set NO_START=0 in /etc/default/jetty, also listen host to 0.0.0.0
		addChild(TemplatedFile.build(template, new File("/etc/default/jetty"), "etc.default.jetty"));

		addChild(injected(AppsContainer.class));

		addChild(ManagedService.build("jetty"));
	}

	public void addApp(Object app) throws OpsException {
		getChild(AppsContainer.class).addChild(app);
	}

}
