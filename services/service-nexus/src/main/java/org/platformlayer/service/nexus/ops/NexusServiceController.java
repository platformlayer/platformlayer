package org.platformlayer.service.nexus.ops;

import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.ops.JettyInstance;
import org.platformlayer.service.nexus.model.NexusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusServiceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(NexusServiceController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		NexusService model = OpsContext.get().getInstance(NexusService.class);

		InstanceBuilder vm;

		{
			vm = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.minimumMemoryMb = 2048;
			addChild(vm);
		}

		vm.addChild(NexusBootstrap.build());

		JettyInstance jetty = vm.addChild(injected(JettyInstance.class));
		jetty.addApp(NexusApp.build());

		vm.addChild(MetricsInstance.class);
	}

}
