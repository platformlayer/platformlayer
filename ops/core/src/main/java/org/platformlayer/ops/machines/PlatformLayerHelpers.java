package org.platformlayer.ops.machines;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.common.Tagset;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.ops.OpsException;

import com.google.common.base.Objects;

public class PlatformLayerHelpers extends TypedPlatformLayerClient {
	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	public UUID getOrCreateUuid(ItemBase model) throws PlatformLayerClientException {
		Tagset tags = model.getTags();
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

	public void setUniqueTags(PlatformLayerKey key, Tag... setTags) throws OpsException {
		UntypedItem itemUntyped = getItemUntyped(key);
		Tagset tags = itemUntyped.getTags();

		TagChanges tagChanges = new TagChanges();
		for (Tag setTag : setTags) {
			String tagKey = setTag.getKey();

			List<String> existing = tags.findAll(tagKey);
			if (existing == null || existing.isEmpty()) {
				tagChanges.addTags.add(setTag);
			} else if (existing.size() == 1) {
				String existingValue = existing.get(0);
				if (!Objects.equal(existingValue, setTag.value)) {
					tagChanges.addTags.add(setTag);
					tagChanges.removeTags.add(Tag.build(tagKey, existingValue));
				}
			} else {
				// We probably should replace existing tags...
				throw new OpsException("Found duplicate tag for: " + setTag.key);
			}
		}

		if (!tagChanges.isEmpty()) {
			changeTags(key, tagChanges);
		}
	}

}
