package org.platformlayer.ops.machines;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;

public class PlatformLayerHelpers extends TypedPlatformLayerClient {
	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	public PlatformLayerHelpers(PlatformLayerClient platformLayerClient, ServiceProviderHelpers serviceProviderHelpers) {
		super(platformLayerClient, new PlatformLayerTypedItemMapper(serviceProviderHelpers));
		this.serviceProviderHelpers = serviceProviderHelpers;
	}

	public static PlatformLayerHelpers build(PlatformLayerClient client) {
		ServiceProviderHelpers serviceProviderHelpers = Injection.getInstance(ServiceProviderHelpers.class);
		return new PlatformLayerHelpers(client, serviceProviderHelpers);
	}

	public <T extends ItemBase> T refresh(T item) throws OpsException {
		item = getItem(OpsSystem.toKey(item));
		return item;
	}

	public <T extends ItemBase> T ensureDeleted(T item) throws OpsException {
		if (item.getState() == ManagedItemState.DELETED) {
			return item;
		}

		try {
			deleteItem(OpsSystem.toKey(item));
		} catch (PlatformLayerClientException e) {
			throw new OpsException("Error deleting persistent instance", e);
		}
		item = refresh(item);
		if (item.getState() != ManagedItemState.DELETED) {
			throw new OpsException("Item not yet deleted");
		}
		return item;
	}

}
