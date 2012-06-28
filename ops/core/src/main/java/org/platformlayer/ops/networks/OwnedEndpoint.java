package org.platformlayer.ops.networks;

import javax.inject.Inject;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.tree.OwnedItem;

public class OwnedEndpoint extends OwnedItem<PublicEndpointBase> {
	public int publicPort;
	public int backendPort;
	public PlatformLayerKey parentItem;

	public Transport transport = null;

	@Inject
	PlatformLayerCloudHelpers platformLayerCloudHelpers;

	@Override
	protected PublicEndpointBase buildItemTemplate() throws OpsException {
		InstanceBase instance = OpsContext.get().getInstance(InstanceBase.class);

		PlatformLayerKey instanceKey = instance.getKey();

		PublicEndpointBase publicEndpoint = platformLayerCloudHelpers.createPublicEndpoint(instance, parentItem);
		// publicEndpoint.network = network;
		publicEndpoint.publicPort = publicPort;
		publicEndpoint.backendPort = backendPort;
		publicEndpoint.instance = instanceKey;
		publicEndpoint.key = PlatformLayerKey.fromId(instance.getId() + "_" + publicPort);

		if (transport != null) {
			publicEndpoint.transport = transport.toString();
		}
		// publicEndpoint.getTags().add(OpsSystem.get().createParentTag(instance));

		Tag uniqueTag = UniqueTag.build(instance, String.valueOf(publicPort));
		publicEndpoint.getTags().add(uniqueTag);

		return publicEndpoint;
	}

}
