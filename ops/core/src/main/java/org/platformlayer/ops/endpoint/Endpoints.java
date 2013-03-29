package org.platformlayer.ops.endpoint;

import java.net.InetAddress;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.networks.NetworkPoints;

import com.google.common.collect.Lists;

public class Endpoints {

	@Inject
	NetworkPoints networks;

	public List<InetAddress> findEndpoints(NetworkPoint src, ItemBase item, int port) throws OpsException {
		// We assume that private networks can still reach the public internet, so these work for everyone
		List<InetAddress> matches = Lists.newArrayList();

		{
			List<EndpointInfo> endpoints = EndpointInfo.findEndpoints(item.getTags(), port);
			for (EndpointInfo endpoint : endpoints) {
				matches.add(endpoint.getAddress());
			}
		}

		{
			NetworkPoint target = networks.findNetworkPoint(item);
			List<InetAddress> addresses = target.findAddresses(src);
			matches.addAll(addresses);
		}

		return matches;
	}
}
