package org.platformlayer.ops.machines;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.collect.Lists;

public class NetworkAddress {

	private final InetAddress inetAddress;

	public NetworkAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public static List<NetworkAddress> find(Tags tags) {
		List<NetworkAddress> addresses = Lists.newArrayList();

		for (String address : tags.find(Tag.NETWORK_ADDRESS)) {
			addresses.add(NetworkAddress.parse(address));
		}

		return addresses;
	}

	private static NetworkAddress parse(String address) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Error resolving network address", e);
		}
		return new NetworkAddress(inetAddress);
	}

	public static NetworkAddress pickBest(List<NetworkAddress> candidates, Strategy<NetworkAddress> strategy) {
		NetworkAddress best = null;
		for (NetworkAddress candidate : candidates) {
			if (best == null) {
				best = candidate;
			} else {
				best = strategy.choose(best, candidate);
			}
		}

		return best;
	}

	public boolean isIpv6() {
		return inetAddress instanceof Inet6Address;
	}

	@Override
	public String toString() {
		return "NetworkAddress [inetAddress=" + inetAddress + "]";
	}

	public String getHostAddress() {
		return inetAddress.getHostAddress();
	}

}
