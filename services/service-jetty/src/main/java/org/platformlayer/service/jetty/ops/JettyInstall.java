package org.platformlayer.service.jetty.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;

public class JettyInstall extends OpsTreeBase {
	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(JavaVirtualMachine.buildJava6());

		addChild(PackageDependency.build("jetty"));

		addChild(injected(JettyBootstrap.class));
	}

}
