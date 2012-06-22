package org.platformlayer.service.platformlayer.ops.auth.user;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.platformlayer.model.UserAuthService;

public class UserAuthServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(UserAuthServiceController.class);

	public static final int PORT = 5000;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		UserAuthInstanceModel template = injected(UserAuthInstanceModel.class);
		UserAuthService model = template.getModel();

		int port = PORT;

		String dnsName = model.dnsName;

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.publicPorts.add(port);

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		{
			UserAuthInstall install = injected(UserAuthInstall.class);
			vm.addChild(install);
		}

		{
			UserAuthInstance service = injected(UserAuthInstance.class);
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
