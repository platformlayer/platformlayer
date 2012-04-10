package org.platformlayer.ops.items;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.Filter;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

public abstract class OwnedItem {
    @Inject
    PlatformLayerHelpers platformLayer;

    @Handler
    public void handler() throws OpsException {
        ItemBase itemTemplate = buildItemTemplate();
        Tag uniqueTag = getUniqueTag(itemTemplate);

        if (OpsContext.isConfigure()) {
            try {
                platformLayer.putItemByTag(itemTemplate, uniqueTag);
            } catch (PlatformLayerClientException e) {
                throw new OpsException("Error creating owned item", e);
            }
        }

        if (OpsContext.isDelete()) {
            List<? extends ItemBase> items = platformLayer.listItems(itemTemplate.getClass(), Filter.byTag(uniqueTag));
            if (items.size() != 0) {
                if (items.size() != 1) {
                    throw new OpsException("Found multiple items with unique tag: " + uniqueTag);
                }

                try {
                    platformLayer.deleteItem(items.get(0).getKey());
                } catch (PlatformLayerClientException e) {
                    throw new OpsException("Error deleting owned item", e);
                }
            }
        }
    }

    protected abstract ItemBase buildItemTemplate() throws OpsException;

    protected Tag getUniqueTag(ItemBase item) throws OpsException {
        for (Tag tag : item.getTags()) {
            if (tag.getKey().equals(Tag.UNIQUE_ID))
                return tag;
        }

        throw new OpsException("Cannot find unique tag for item: " + item);
    }
}
