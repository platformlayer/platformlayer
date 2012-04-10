package org.platformlayer.service.zookeeper.ops;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Filter;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.UniqueTag;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.MachineCluster;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.collect.Lists;

public class ZookeeperClusterController extends OpsTreeBase implements MachineCluster {
	static final Logger log = Logger
			.getLogger(ZookeeperClusterController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void handler(ZookeeperCluster model) throws OpsException {
		Tag parentTag = Tag.buildParentTag(model.getKey());
		List<ZookeeperServer> servers = platformLayer.listItems(
				ZookeeperServer.class, Filter.byTag(parentTag));

		// Create servers so that we hit the desired cluster size
		int clusterSize = 1;
		if (servers.size() < clusterSize) {
			for (int i = servers.size(); i < clusterSize; i++) {
				ZookeeperServer server = new ZookeeperServer();
				
				String clusterId = String.valueOf(i);

				server.clusterDnsName = model.dnsName;
				server.clusterId = clusterId;
				
				Tag uniqueTag = UniqueTag.build(model, clusterId);
				server.getTags().add(uniqueTag);
				server.getTags().add(parentTag);

				server.key = PlatformLayerKey.fromId(model.getId() + "-" + clusterId);

				ZookeeperServer created = platformLayer.putItemByTag(server,
						uniqueTag);
				log.info("Created zookeeper server: " + created);
			}
		}
	}

	@Override
	protected void addChildren() throws OpsException {

	}

    @Override
    public List<Machine> getMachines(Object modelObject) throws OpsException {
        ZookeeperCluster model = (ZookeeperCluster) modelObject;
        Filter parentFilter = Filter.byTag(Tag.buildParentTag(model.getKey()));

        List<Machine> machines = Lists.newArrayList();

        for (ZookeeperServer server : platformLayer.listItems(ZookeeperServer.class, parentFilter)) {
            Machine machine = instances.getMachine(server);

            machines.add(machine);
        }

        return machines;
    }
}
