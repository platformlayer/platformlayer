package org.platformlayer.ops.machines;

import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.OpsException;

public class PlatformLayerHelpers extends TypedPlatformLayerClient {
	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	public UUID getOrCreateUuid(ItemBase model) throws PlatformLayerClientException {
		Tags tags = model.getTags();
		UUID uuid = Tag.UUID.findUnique(tags);
		if (uuid != null) {
			return uuid;
		}

		uuid = UUID.randomUUID();
		Tag uuidTag = Tag.UUID.build(uuid);
		tags.add(uuidTag);
		this.addTag(model.getKey(), uuidTag);
		return uuid;
	}

	@Inject
	public PlatformLayerHelpers(PlatformLayerClient platformLayerClient, ServiceProviderHelpers serviceProviderHelpers) {
		super(platformLayerClient, new PlatformLayerTypedItemMapper(serviceProviderHelpers));
		this.serviceProviderHelpers = serviceProviderHelpers;
	}

	public <T extends ItemBase> T refresh(T item) throws OpsException {
		item = getItem(item.getKey());
		return item;
	}

	public <T extends ItemBase> T ensureDeleted(T item) throws OpsException {
		if (item.getState() == ManagedItemState.DELETED) {
			return item;
		}

		try {
			deleteItem(item.getKey());
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
