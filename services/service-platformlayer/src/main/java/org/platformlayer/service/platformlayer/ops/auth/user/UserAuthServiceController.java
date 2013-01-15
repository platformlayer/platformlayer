package org.platformlayer.service.platformlayer.ops.auth.user;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.platformlayer.model.UserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAuthServiceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(UserAuthServiceController.class);

	public static final int PORT = 5001;

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
			vm = InstanceBuilder.build(dnsName, this);
			vm.publicPorts.add(port);
			vm.hostPolicy.configureCluster(template.getPlacementKey());

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
