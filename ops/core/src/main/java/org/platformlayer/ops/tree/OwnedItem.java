package org.platformlayer.ops.tree;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TagFilter;
import org.platformlayer.common.IsTag;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

public abstract class OwnedItem<T extends ItemBase> {
	@Inject
	PlatformLayerHelpers platformLayer;

	private T item;

	public T getItem() {
		return item;
	}

	@Handler
	public void handler() throws OpsException {
		T itemTemplate = buildItemTemplate();
		IsTag uniqueTag = getUniqueTag(itemTemplate);

		if (OpsContext.isConfigure()) {
			try {
				item = platformLayer.putItemByTag(itemTemplate, uniqueTag);
			} catch (PlatformLayerClientException e) {
				throw new OpsException("Error creating owned item", e);
			}
		}

		if (OpsContext.isDelete()) {
			List<? extends ItemBase> items = platformLayer.listItems(itemTemplate.getClass(),
					TagFilter.byTag(uniqueTag));
			if (items.size() != 0) {
				if (items.size() != 1) {
					throw new OpsException("Found multiple items with unique tag: " + uniqueTag);
				}

				item = (T) items.get(0);

				platformLayer.ensureDeleted(item);
			}
		}
	}

	protected abstract T buildItemTemplate() throws OpsException;

	protected IsTag getUniqueTag(ItemBase item) throws OpsException {
		for (IsTag tag : item.getTags()) {
			if (tag.getKey().equals(Tag.UNIQUE_ID)) {
				return tag;
			}
		}

		throw new OpsException("Cannot find unique tag for item: " + item);
	}
}
