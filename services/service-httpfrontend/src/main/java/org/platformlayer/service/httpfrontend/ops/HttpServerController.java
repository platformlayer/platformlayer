package org.platformlayer.service.httpfrontend.ops;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(HttpServerController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		HttpServer model = OpsContext.get().getInstance(HttpServer.class);

		InstanceBuilder vm = InstanceBuilder.build(model.dnsName, this, model.getTags());
		vm.addTagToManaged = true;
		vm.publicPorts.add(HttpHelpers.PORT);

		// TODO: Do we need a Cluster concept?
		// For now, we fake it
		PlatformLayerKey key = model.getKey();
		String groupId = key.withId(new ManagedItemId("primary")).getUrl();
		groupId = groupId.replace("/httpServer/", "/httpCluster/");
		vm.hostPolicy.configureSpread(groupId);

		// vm.minimumMemoryMb = 512;

		addChild(vm);

		vm.addChild(HttpServerInstall.class);

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = HttpHelpers.PORT;
			endpoint.backendPort = HttpHelpers.PORT;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			vm.addChild(endpoint);
		}

		vm.addChild(HttpServerInstance.class);
	}
}
