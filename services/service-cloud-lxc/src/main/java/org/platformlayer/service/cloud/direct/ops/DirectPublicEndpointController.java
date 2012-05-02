package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectPublicEndpoint;

public class DirectPublicEndpointController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(DirectPublicEndpointController.class);

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
			publicPorts.uuid = getUuid(model);

			publicPorts.backendPort = model.backendPort;
			publicPorts.publicPort = model.publicPort;
			addChild(publicPorts);
		}
	}

	private String getUuid(DirectPublicEndpoint model) throws OpsException {
		Tags tags = model.getTags();
		String uuid = tags.findUnique(Tag.UUID);
		if (uuid != null) {
			return uuid;
		}

		uuid = UUID.randomUUID().toString();
		Tag uuidTag = new Tag(Tag.UUID, uuid);
		tags.add(uuidTag);
		platformLayerClient.addTag(OpsSystem.toKey(model), uuidTag);
		return uuid;
	}

}
