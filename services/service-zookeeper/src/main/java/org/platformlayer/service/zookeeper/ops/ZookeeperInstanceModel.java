package org.platformlayer.service.zookeeper.ops;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ZookeeperInstanceModel extends StandardTemplateData {
	static final Logger log = Logger.getLogger(ZookeeperInstanceModel.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Override
	public ZookeeperServer getModel() {
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
			String address = sourceMachine.getBestAddress(targetNetworkPoint, ZookeeperConstants.ZK_SYSTEM_PORT_1);

			model.ip = address;

			cluster.servers.add(model);
		}
		return cluster;
	}

	public List<ZookeeperServer> getClusterServers() throws OpsException {
		ZookeeperServer model = getModel();

		Tag parentTag = Tag.PARENT.findUniqueTag(model);
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

	@Override
	public File getInstallDir() {
		return new File("/opt/zookeeper/zookeeper-3.3.5/zookeeper-3.3.5");
	}

	@Override
	public File getInstanceDir() {
		return new File(new File("/var/zookeeper"), getInstanceKey());
	}

	@Override
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

	private ZookeeperCluster getClusterModel() throws OpsException {
		ZookeeperServer model = getModel();

		PlatformLayerKey parentKey = Tag.PARENT.findUnique(model);
		if (parentKey == null) {
			throw new OpsException("Parent tag not set on Zookeeper server");
		}

		ZookeeperCluster cluster = platformLayer.getItem(parentKey, ZookeeperCluster.class);
		return cluster;
	}

	@Override
	public String getKey() {
		return "zookeeper";
	}

	@Override
	protected Command getCommand() throws OpsException {
		JavaCommandBuilder builder = new JavaCommandBuilder();
		builder.setMainClass("org.apache.zookeeper.server.quorum.QuorumPeerMain");
		builder.addArgument(Argument.buildFile(getConfigurationFile()));
		builder.addClasspath(getInstanceDir(), false);
		builder.addClasspathFolder(getInstallDir());
		builder.addClasspathFolder(new File(getInstallDir(), "lib"));

		return builder.get();
		// directory=${instanceDir}
		// command=java -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -cp
		// "${instanceDir}:${installDir}/*:${installDir}/lib/*" org.apache.zookeeper.server.quorum.QuorumPeerMain
		// zookeeper.cfg
	}

	@Override
	protected Map<String, String> getConfigurationProperties() throws OpsException {
		Map<String, String> config = Maps.newHashMap();

		// The number of milliseconds of each tick
		config.put("tickTime", "2000");

		// The number of ticks that the initial
		// synchronization phase can take
		config.put("initLimit", "10");

		// The number of ticks that can pass between
		// sending a request and getting an acknowledgment
		config.put("syncLimit", "5");

		// the directory where the snapshot is stored.
		config.put("dataDir", getDataDir().getAbsolutePath());

		// the port at which the clients will connect
		config.put("clientPort", "2181");

		Cluster cluster = getCluster();

		if (cluster.servers.size() == 1) {
			// Server list ommitted to side-step ZOOKEEPER-1460 (for now)
		} else {
			for (ClusterServer server : cluster.servers) {
				config.put("server." + server.key, server.ip + ":2888:2889");
			}
		}

		return config;
	}

	@Override
	protected PlatformLayerKey getSslKeyPath() {
		return null;
	}

	public File getDataDir() {
		return new File(getInstanceDir(), "data");
	}

	public File getLogsDir() {
		return new File(getInstanceDir(), "logs");
	}

	@Override
	public File getConfigurationFile() {
		return new File(getInstanceDir(), "zookeeper.cfg");
	}

}
