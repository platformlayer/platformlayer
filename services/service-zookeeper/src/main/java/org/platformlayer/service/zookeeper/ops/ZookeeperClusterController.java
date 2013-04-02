package org.platformlayer.service.zookeeper.ops;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ZookeeperClusterController extends OpsTreeBase implements MachineCluster {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperClusterController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Bound
	ZookeeperCluster model;

	@Inject
	ZookeeperUtils zookeeper;

	@Handler
	public void handler() throws OpsException {

	}

	static class ZookeeperChildServer extends OwnedItem<ZookeeperServer> {
		ZookeeperCluster cluster;
		String clusterId;

		@Override
		protected ZookeeperServer buildItemTemplate() throws OpsException {
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
		for (int i = 0; i < model.clusterSize; i++) {
			ZookeeperChildServer childServer = addChild(ZookeeperChildServer.class);
			childServer.cluster = model;
			childServer.clusterId = String.valueOf(i);
		}

		{
			TagFromChildren tagger = addChild(TagFromChildren.class);
			tagger.parentItem = model;
			tagger.parentController = this;
			// tagger.ownedItemType = ZookeeperChildServer.class;
			tagger.port = ZookeeperConstants.ZK_PUBLIC_PORT;
		}
	}

	@Override
	public List<Machine> getMachines(boolean required) throws OpsException {
		List<Machine> machines = Lists.newArrayList();

		for (ZookeeperServer server : zookeeper.getServers(model)) {
			Machine machine = instances.getMachine(server, required);
			if (machine != null) {
				machines.add(machine);
			}
		}

		return machines;
	}
}
