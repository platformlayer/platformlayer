package org.platformlayer.service.solr.ops;

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
import org.platformlayer.service.solr.model.SolrServer;
import org.platformlayer.service.solr.ops.SolrClusterController.SolrChildServer;

public class TagFromChildren {
	ItemBase parentItem;
	OpsTreeBase parentController;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Handler
	public void handler() throws OpsException {
		if (OpsContext.isConfigure()) {
			for (SolrChildServer childServer : parentController.getChildren(SolrChildServer.class)) {
				SolrServer server = childServer.getItem();
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
