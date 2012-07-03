package org.platformlayer.service.platformlayer.ops.auth.system;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.platformlayer.model.SystemAuthService;

public class SystemAuthServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(SystemAuthServiceController.class);

	public static final int PORT = 35358;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		SystemAuthInstanceModel template = injected(SystemAuthInstanceModel.class);
		SystemAuthService model = template.getModel();

		int port = PORT;

		String dnsName = model.dnsName;

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.publicPorts.add(port);
			vm.hostPolicy.configureCluster(template.getPlacementKey());

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		{
			SystemAuthInstall install = injected(SystemAuthInstall.class);
			vm.addChild(install);
		}

		{
			SystemAuthInstance service = injected(SystemAuthInstance.class);
			vm.addChild(service);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			vm.addChild(endpoint);
		}
	}
}
