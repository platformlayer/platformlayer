package org.platformlayer.ops.tagger;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsSystem;

public class Tagger {
	static final Logger log = Logger.getLogger(Tagger.class);

	public PlatformLayerKey platformLayerKey;
	public OpsProvider<TagChanges> tagChangesProvider;

	public static Tagger build(ItemBase item, OpsProvider<TagChanges> tagChanges) {
		OpsContext opsContext = OpsContext.get();
		Tagger tagger = opsContext.getInjector().getInstance(Tagger.class);
		tagger.platformLayerKey = OpsSystem.toKey(item);
		tagger.tagChangesProvider = tagChanges;
		return tagger;
	}

	@Inject
	PlatformLayerClient platformLayer;

	@Handler
	public void handler() throws OpsException {
		if (OpsContext.isDelete() || OpsContext.isConfigure()) {
			TagChanges tagChanges = tagChangesProvider.get();

			if (tagChanges != null) {
				log.info("Setting tags on " + platformLayerKey);

				if (OpsContext.isDelete()) {
					// Swap the tags for a removal
					Tags x = tagChanges.addTags;
					tagChanges.addTags = tagChanges.removeTags;
					tagChanges.removeTags = x;
				}

				platformLayer.changeTags(platformLayerKey, tagChanges);
			}
		}
	}
}
