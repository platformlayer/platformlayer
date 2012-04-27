package org.platformlayer.ops.tagger;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerUtils;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.tree.OwnedItem;

public class TagFromChildren {
	public ItemBase parentItem;
	public OpsTreeBase parentController;
	public Class<? extends OwnedItem> ownedItemType;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() throws OpsException {
		if (OpsContext.isConfigure()) {
			for (OwnedItem childServer : parentController.getChildren(OwnedItem.class)) {
				ItemBase server = childServer.getItem();
				if (server == null) {
					// TODO: It's _possible_ that the child is ready instantly.
					// Right now, we have to go through a retry cycle
					throw new OpsException("Child server not ready");
				}
				List<String> endpoints = PlatformLayerUtils.findEndpoints(server.getTags());
				if (endpoints.isEmpty()) {
					// TODO: Cope in future e.g. if we only need one of two
					throw new OpsException("Child server not ready");
				}

				if (endpoints.size() != 1) {
					throw new OpsException("Expected exactly one endpoint");
				}

				platformLayer.addTag(parentItem.getKey(), new Tag(Tag.PUBLIC_ENDPOINT, endpoints.get(0)));
			}
		}
	}
}
