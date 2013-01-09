package org.platformlayer.service.solr.ops;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.Filter;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.MachineCluster;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tagger.TagFromChildren;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.tree.OwnedItem;
import org.platformlayer.service.solr.model.SolrCluster;
import org.platformlayer.service.solr.model.SolrServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SolrClusterController extends OpsTreeBase implements MachineCluster {

	private static final Logger log = LoggerFactory.getLogger(SolrClusterController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void handler() throws OpsException {
	}

	static class SolrChildServer extends OwnedItem<SolrServer> {
		SolrCluster cluster;
		String clusterId;

		@Override
		protected SolrServer buildItemTemplate() throws OpsException {
			Tag parentTag = Tag.buildParentTag(cluster.getKey());

			SolrServer server = new SolrServer();

			server.clusterId = clusterId;

			Tag uniqueTag = UniqueTag.build(cluster, clusterId);
			server.getTags().add(uniqueTag);
			server.getTags().add(parentTag);

			server.key = PlatformLayerKey.fromId(cluster.getId() + "-" + clusterId);

			return server;
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		SolrCluster model = OpsContext.get().getInstance(SolrCluster.class);

		int clusterSize = 1;
		for (int i = 0; i < clusterSize; i++) {
			SolrChildServer childServer = injected(SolrChildServer.class);
			childServer.cluster = model;
			childServer.clusterId = String.valueOf(i);
			addChild(childServer);
		}

		{
			TagFromChildren tagger = injected(TagFromChildren.class);
			tagger.parentItem = model;
			tagger.parentController = this;
			tagger.ownedItemType = SolrChildServer.class;
			addChild(tagger);
		}
	}

	@Override
	public List<Machine> getMachines(Object modelObject, boolean required) throws OpsException {
		SolrCluster model = (SolrCluster) modelObject;
		Filter parentFilter = TagFilter.byTag(Tag.buildParentTag(model.getKey()));

		List<Machine> machines = Lists.newArrayList();

		for (SolrServer server : platformLayer.listItems(SolrServer.class, parentFilter)) {
			Machine machine = instances.getMachine(server, required);
			if (machine != null) {
				machines.add(machine);
			}
		}

		return machines;
	}
}
