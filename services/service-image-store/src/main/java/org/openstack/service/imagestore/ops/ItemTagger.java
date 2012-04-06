package org.openstack.service.imagestore.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.machines.PlatformLayerHelpers;

import com.google.common.base.Objects;

public class ItemTagger {
    public Tag tag;

    @Inject
    PlatformLayerHelpers platformLayerClient;

    @Handler
    public void handler() throws OpsException {
        ItemBase item = OpsContext.get().getInstance(ItemBase.class);
        String matchingTag = item.getTags().findUnique(tag.key);
        if (!Objects.equal(matchingTag, tag.getValue())) {
            platformLayerClient.addTag(OpsSystem.toKey(item), tag);
        }
    }

    public static ItemTagger build(Tag tag) {
        ItemTagger tagger = Injection.getInstance(ItemTagger.class);
        tagger.tag = tag;
        return tagger;
    }

}
