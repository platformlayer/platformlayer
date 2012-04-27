package org.platformlayer.service.zookeeper.ops;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.collect.Lists;

public class ZookeeperInstanceModel implements TemplateDataSource {
	static final Logger log = Logger.getLogger(ZookeeperInstanceModel.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	ZookeeperServer getModel() {
		ZookeeperServer model = OpsContext.get().getInstance(ZookeeperServer.class);
		return model;
	}

	// A model of the cluster
	public static class Cluster {
		public List<ClusterServer> servers = Lists.newArrayList();
	}

	public static class ClusterServer {
		public String key;
		public String ip;
	}

	public Cluster getCluster() throws OpsException {
		Cluster cluster = new Cluster();
		for (ZookeeperServer server : getClusterServers()) {
			// TODO: Do keys need to be sequential
			ClusterServer model = new ClusterServer();
			model.key = server.clusterId;

			// TODO: What do we do about machines that don't yet have an ip?
			NetworkPoint targetNetworkPoint = NetworkPoint.forPublicInternet();
			Machine sourceMachine = instances.getMachine(server);
			String address = sourceMachine.getAddress(targetNetworkPoint, ZookeeperConstants.ZK_SYSTEM_PORT_1);

			model.ip = address;

			cluster.servers.add(model);
		}
		return cluster;
	}

	public List<ZookeeperServer> getClusterServers() throws OpsException {
		ZookeeperServer model = getModel();

		Tag parentTag = model.getTags().findUniqueTag(Tag.PARENT);
		if (parentTag == null) {
			log.warn("Parent tag not set on Zookeeper server; assuming standalone server");
			return Lists.newArrayList(model);
		}

		List<ZookeeperServer> servers = platformLayer.listItems(ZookeeperServer.class, TagFilter.byTag(parentTag));
		return servers;
	}

	public String getMyId() {
		return getModel().clusterId;
	}

	public File getInstallDir() {
		return new File("/opt/zookeeper/zookeeper-3.3.5/zookeeper-3.3.5");
	}

	public File getInstanceDir() {
		return new File(new File("/var/zookeeper"), getInstanceKey());
	}

	public String getInstanceKey() {
		// TOOD: Should this be the cluster unique id??
		String instanceKey = "default";
		return instanceKey;
	}

	@Override
	public void buildTemplateModel(Map<String, Object> model) throws OpsException {
		// TODO: Build by reflection? Rely on bean reflection in our templating
		// library?

		model.put("installDir", getInstallDir());
		model.put("instanceDir", getInstanceDir());
		model.put("cluster", getCluster());
		model.put("myid", getMyId());
	}

}
