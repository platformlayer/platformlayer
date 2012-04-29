package org.platformlayer.service.zookeeper.ops;

import java.util.List;

import org.platformlayer.service.zookeeper.model.ZookeeperCluster;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

import com.google.common.collect.Lists;

public class ZookeeperUtils {

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

}
