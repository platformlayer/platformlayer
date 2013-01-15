package org.platformlayer.service.tomcat.ops;

import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.java.JavaVirtualMachine;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.tomcat.model.TomcatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatServiceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(TomcatServiceController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		TomcatService model = OpsContext.get().getInstance(TomcatService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName, this);
		instance.minimumMemoryMb = 2048;
		addChild(instance);

		instance.addChild(JavaVirtualMachine.buildJava6());

		instance.addChild(PackageDependency.build("libtcnative-1"));
		instance.addChild(PackageDependency.build("tomcat6"));
		// tomcat6-admin contains the 'manager' app for remote deploys
		instance.addChild(PackageDependency.build("tomcat6-admin"));

		instance.addChild(TomcatUsers.build());

		instance.addChild(TomcatServerBootstrap.build());

		instance.addChild(MetricsInstance.class);

		instance.addChild(ManagedService.build("tomcat6"));
	}
}
