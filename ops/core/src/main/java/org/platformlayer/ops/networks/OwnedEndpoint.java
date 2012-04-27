package org.platformlayer.ops.networks;

import javax.inject.Inject;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.tree.OwnedItem;

public class OwnedEndpoint extends OwnedItem<PublicEndpointBase> {
	public int publicPort;
	public int backendPort;
	public PlatformLayerKey parentItem;

	@Inject
	PlatformLayerCloudHelpers platformLayerCloudHelpers;

	@Override
	protected PublicEndpointBase buildItemTemplate() throws OpsException {
		InstanceBase instance = OpsContext.get().getInstance(InstanceBase.class);

		PlatformLayerKey instanceKey = OpsSystem.toKey(instance);

		PublicEndpointBase publicEndpoint = platformLayerCloudHelpers.createPublicEndpoint(instance, parentItem);
		// publicEndpoint.network = network;
		publicEndpoint.publicPort = publicPort;
		publicEndpoint.backendPort = backendPort;
		publicEndpoint.instance = instanceKey;
		publicEndpoint.key = PlatformLayerKey.fromId(instance.getId() + "_" + publicPort);

		// publicEndpoint.getTags().add(OpsSystem.get().createParentTag(instance));

		Tag uniqueTag = UniqueTag.build(instance, String.valueOf(publicPort));
		publicEndpoint.getTags().add(uniqueTag);

		return publicEndpoint;
	}

}
