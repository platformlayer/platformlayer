package org.platformlayer.service.zookeeper.ops;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Filter;
import org.platformlayer.core.model.ItemBase;
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
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.collect.Lists;

public class ZookeeperClusterController extends OpsTreeBase implements MachineCluster {
	static final Logger log = Logger.getLogger(ZookeeperClusterController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void handler() throws OpsException {

	}

	static class ZookeeperChildServer extends OwnedItem {
		ZookeeperCluster cluster;
		String clusterId;

		@Override
		protected ItemBase buildItemTemplate() throws OpsException {
			Tag parentTag = Tag.buildParentTag(cluster.getKey());

			ZookeeperServer server = new ZookeeperServer();

			server.clusterDnsName = cluster.dnsName;
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
		ZookeeperCluster model = OpsContext.get().getInstance(ZookeeperCluster.class);

		for (int i = 0; i < model.clusterSize; i++) {
			ZookeeperChildServer childServer = injected(ZookeeperChildServer.class);
			childServer.cluster = model;
			childServer.clusterId = String.valueOf(i);
			addChild(childServer);
		}

		{
			TagFromChildren tagger = injected(TagFromChildren.class);
			tagger.parentItem = model;
			tagger.parentController = this;
			tagger.ownedItemType = ZookeeperChildServer.class;
			tagger.port = ZookeeperConstants.ZK_PUBLIC_PORT;
			addChild(tagger);
		}
	}

	@Override
	public List<Machine> getMachines(Object modelObject, boolean required) throws OpsException {
		ZookeeperCluster model = (ZookeeperCluster) modelObject;
		Filter parentFilter = Filter.byTag(Tag.buildParentTag(model.getKey()));

		List<Machine> machines = Lists.newArrayList();

		for (ZookeeperServer server : platformLayer.listItems(ZookeeperServer.class, parentFilter)) {
			Machine machine = instances.getMachine(server, required);
			if (machine != null) {
				machines.add(machine);
			}
		}

		return machines;
	}
}
