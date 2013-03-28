package org.platformlayer.service.zookeeper.ops;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.platformlayer.TagFilter;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.java.JavaCommandBuilder;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.standardservice.StandardTemplateData;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ZookeeperInstanceModel extends StandardTemplateData {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperInstanceModel.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instances;

	@Bound
	ZookeeperServer model;

	@Override
	public ZookeeperServer getModel() {
		return model;
	}

	// A model of the cluster
	public static class Cluster {
		public List<ClusterServer> servers = Lists.newArrayList();
	}

	public static class ClusterServer {
		public String key;
		public String ip;
		public String dnsName;
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

			model.dnsName = ZookeeperUtils.buildDnsName(server);

			cluster.servers.add(model);
		}
		return cluster;
	}

	public List<ZookeeperServer> getClusterServers() throws OpsException {
		PlatformLayerKey parent = getClusterKey();
		if (parent == null) {
			log.warn("Parent tag not set on Zookeeper server; assuming standalone server");
			return Lists.newArrayList(model);
		}

		List<ZookeeperServer> servers = platformLayer.listItems(ZookeeperServer.class,
				TagFilter.byTag(Tag.PARENT.build(parent)));
		return servers;
	}

	private PlatformLayerKey getClusterKey() {
		PlatformLayerKey parent = Tag.PARENT.findUnique(model);
		return parent;
	}

	public String getMyId() {
		return getModel().clusterId;
	}

	@Override
	public File getInstallDir() {
		// TODO: Should we define getDownloadDir instead, with a default delegation??
		return new File("/opt/zookeeper/");
	}

	public File getBaseDir() {
		return new File(getInstallDir(), "zookeeper-3.4.5");
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

		model.put("installDir", getBaseDir());
		model.put("instanceDir", getInstanceDir());
		model.put("cluster", getCluster());
		model.put("myid", getMyId());
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
		builder.addClasspathFolder(getBaseDir());
		builder.addClasspathFolder(new File(getBaseDir(), "lib"));

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
				String host = server.ip;

				// We use DNS names to side-step ZOOKEEPER-1460
				host = server.dnsName;

				config.put("server." + server.key, host + ":2888:2889");
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

	public String getClusterGroupId() throws OpsException {
		return getClusterKey().toString();
	}

	@Override
	public String getDownloadSpecifier() {
		return "zookeeper-3.4.5.tar.gz";
	}

}
