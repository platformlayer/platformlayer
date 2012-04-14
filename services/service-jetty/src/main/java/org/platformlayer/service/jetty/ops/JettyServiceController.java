package org.platformlayer.service.jetty.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.model.JettyService;

public class JettyServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(JettyServiceController.class);

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		JettyService model = OpsContext.get().getInstance(JettyService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		// TODO: Make configurable
		instance.minimumMemoryMb = 2048;
		addChild(instance);

		instance.addChild(injected(JettyInstall.class));

		instance.addChild(injected(JettyInstance.class));

		instance.addChild(CollectdCollector.build());
	}
}
