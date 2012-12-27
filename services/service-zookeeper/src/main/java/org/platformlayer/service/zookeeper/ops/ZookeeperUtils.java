package org.platformlayer.service.zookeeper.ops;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Filter;
import org.platformlayer.TagFilter;
import org.platformlayer.TimeSpan;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.fathomdb.io.IoUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ZookeeperUtils {
	private static final Logger log = Logger.getLogger(ZookeeperUtils.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	public static String buildDnsName(ZookeeperServer model) {
		return buildDnsName(model.clusterId, model.clusterDnsName);
	}

	public static List<String> getDnsNames(ZookeeperCluster model) {
		List<String> dnsNames = Lists.newArrayList();
		for (int i = 0; i < model.clusterSize; i++) {
			dnsNames.add(buildDnsName(String.valueOf(i), model.dnsName));
		}

		return dnsNames;
	}

	public static String buildDnsName(String clusterId, String clusterDnsName) {
		return "s" + clusterId + "-" + clusterDnsName;
	}

	public static class ZookeeperResponse {
		final String raw;

		public ZookeeperResponse(String raw) {
			this.raw = raw;
		}

		public String getRaw() {
			return raw;
		}

		public Iterable<String> asLines() {
			return Splitter.on('\n').split(raw);
		}

		public Map<String, String> asMap() {
			// return Splitter.on('\n').trimResults().withKeyValueSeparator(":").split(raw);
			Map<String, String> map = Maps.newHashMap();
			for (String line : asLines()) {
				int colonIndex = line.indexOf(':');
				if (colonIndex != -1) {
					String key = line.substring(0, colonIndex).trim();
					String value = line.substring(colonIndex + 1).trim();
					map.put(key, value);
				}
			}
			return map;
		}
	}

	public static ZookeeperResponse sendCommand(InetSocketAddress socketAddress, String command) throws IOException {
		TimeSpan connectionTimeout = TimeSpan.TEN_SECONDS;

		Socket s = new Socket();
		s.setTcpNoDelay(true);
		s.setSoTimeout((int) connectionTimeout.getTotalMilliseconds());

		s.connect(socketAddress);

		s.getOutputStream().write(command.getBytes());
		s.getOutputStream().flush();

		// TODO: Timeout?
		String response = IoUtils.readAll(s.getInputStream());

		return new ZookeeperResponse(response);
	}

	public static ZookeeperResponse sendCommand(OpsTarget target, InetSocketAddress socketAddress, String zkCommand)
			throws OpsException {
		Command command = Command.build("echo {0} | nc {1} {2}", zkCommand,
				socketAddress.getAddress().getHostAddress(), socketAddress.getPort());

		ProcessExecution execution = target.executeCommand(command);

		return new ZookeeperResponse(execution.getStdOut());
	}

	public List<ZookeeperServer> getServers(ZookeeperCluster model) throws OpsException {
		Filter parentFilter = TagFilter.byTag(Tag.buildParentTag(model.getKey()));
		return Lists.newArrayList(platformLayer.listItems(ZookeeperServer.class, parentFilter));
	}

}
