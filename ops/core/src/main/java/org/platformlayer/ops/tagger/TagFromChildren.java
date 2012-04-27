package org.platformlayer.ops.tagger;

import javax.inject.Inject;

import org.platformlayer.EndpointInfo;
import org.platformlayer.core.model.ItemBase;
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
	public Integer port = null;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() throws OpsException {
		if (OpsContext.isConfigure()) {
			for (OwnedItem<?> childServer : parentController.getChildren(OwnedItem.class)) {
				ItemBase server = childServer.getItem();
				if (server == null) {
					// TODO: It's _possible_ that the child is ready instantly.
					// Right now, we have to go through a retry cycle
					throw new OpsException("Child server not ready");
				}
				EndpointInfo endpoint = EndpointInfo.findEndpoint(server.getTags(), port);
				if (endpoint == null) {
					// TODO: Cope in future e.g. if we only need one of two in a cluster
					throw new OpsException("Child server not ready");
				}

				// if (endpoints.size() != 1) {
				// throw new OpsException("Expected exactly one endpoint");
				// }

				platformLayer.addTag(parentItem.getKey(), endpoint.toTag());
			}
		}
	}
}
