package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.EnumUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectPublicEndpointController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(DirectPublicEndpointController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Override
	protected void addChildren() throws OpsException {
		DirectPublicEndpoint model = OpsContext.get().getInstance(DirectPublicEndpoint.class);
		DirectInstance directInstance = platformLayerClient.getItem(model.instance, DirectInstance.class);

		{
			PublicPorts publicPorts = injected(PublicPorts.class);
			publicPorts.backendItem = directInstance;
			publicPorts.tagItems.add(directInstance);
			publicPorts.tagItems.add(model);
			publicPorts.uuid = platformLayerClient.getOrCreateUuid(model).toString();
			publicPorts.backendPort = model.backendPort;
			publicPorts.publicPort = model.publicPort;

			if (model.transport != null) {
				publicPorts.transport = EnumUtils.valueOfCaseInsensitive(Transport.class, model.transport);
			}

			addChild(publicPorts);
		}
	}

}
