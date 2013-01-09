package org.platformlayer.ops.networks;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import org.slf4j.*;
import org.platformlayer.InetAddressChooser;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.packages.AsBlock;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class NetworkPoint {
	private static final Logger log = LoggerFactory.getLogger(NetworkPoint.class);

	final String privateNetworkId;
	final InetAddress address;

	// // For now, we assume there's one private network
	// public static final String PRIVATE_NETWORK_ID = "private";

	private NetworkPoint(String privateNetworkId, InetAddress address) {
		this.privateNetworkId = privateNetworkId;
		this.address = address;
	}

	// public static String getMyNetworkKey() {
	// // We assume we're on the private network
	// return PRIVATE_NETWORK_ID;
	// }

	// public static NetworkPoint forSameNetwork(InetAddress address) {
	// return new NetworkPoint(getMyNetworkKey(), address);
	// }

	public static NetworkPoint forSshAddress(InetAddress addr) {
		if (InetAddressUtils.isIpv6(addr)) {
			return new NetworkPoint(null, addr);
		}

		// This isn't technically required, but IPV6 means that we have a flat address space
		throw new IllegalStateException();
	}

	static InetAddress myAddress;

	public static NetworkPoint forMe() throws OpsException {
		if (myAddress == null) {
			List<InetAddress> localAddresses = InetAddressUtils.getLocalAddresses();

			List<InetAddress> valid = Lists.newArrayList();
			for (InetAddress localAddress : localAddresses) {
				if (InetAddressUtils.isIpv4(localAddress)) {
					continue;
				}

				if (localAddress.isLinkLocalAddress()) {
					continue;
				}
				if (localAddress.isLoopbackAddress()) {
					continue;
				}

				valid.add(localAddress);
			}

			InetAddress address = InetAddressChooser.preferIpv6().choose(valid);

			if (!InetAddressUtils.isIpv6(address)) {
				throw new OpsException("We must have an IPV6 address");
			}
			myAddress = address;
		}

		return new NetworkPoint(null, myAddress);
	}

	public static NetworkPoint forTarget(OpsTarget target) {
		return target.getNetworkPoint();
	}

	public static NetworkPoint forPublicInternet() {
		return new NetworkPoint(null, null);
	}

	public static NetworkPoint forPublicHostname(InetAddress address) {
		String privateNetwork = null;
		if (!InetAddressUtils.isPublic(address)) {
			log.warn("Assigning fake private-network id for non-public IP");
			// Assign a unique private network
			privateNetwork = UUID.randomUUID().toString();
		}
		return new NetworkPoint(privateNetwork, address);
	}

	public static NetworkPoint forPublicHostname(String hostname) throws OpsException {
		InetAddress address;
		try {
			address = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			throw new OpsException("Error resolving hostname", e);
		}

		return forPublicHostname(address);
	}

	public static NetworkPoint forTargetInContext() {
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		return forTarget(target);
	}

	private String getPrivateNetworkId() {
		return privateNetworkId;
	}

	private InetAddress getAddress() {
		return address;
	}

	public boolean isPublicAddress() {
		return this.privateNetworkId == null;
	}

	// private static NetworkPoint forNetwork(String network) {
	// return new NetworkPoint(network, null);
	// }

	@Override
	public String toString() {
		return "NetworkPoint [privateNetworkId=" + privateNetworkId + ", address=" + address.getHostAddress() + "]";
	}

	public List<InetAddress> findReachableAddresses(NetworkPoint src) {
		List<InetAddress> reachables = Lists.newArrayList();

		if (Objects.equal(src.getPrivateNetworkId(), getPrivateNetworkId())) {
			reachables.add(getAddress());
		} else if (isPublicAddress()) {
			reachables.add(getAddress());
		}
		return reachables;
	}

	public static int estimateDistance(NetworkPoint a, NetworkPoint b) {
		if (a.equals(b)) {
			return 0;
		}

		if (a.isPublicAddress() != b.isPublicAddress()) {
			// We need to download from A and then upload to B, so d(A, Me) + d(Me, B)
			// TODO: This is a poor metric. Our metric isn't really rich enough here
			return 8;
		}

		AsBlock asA = AsBlock.find(a.getAddress());
		AsBlock asB = AsBlock.find(b.getAddress());

		if (asA != null && asB != null) {
			if (asA.equals(asB)) {
				return 1;
			}

			if (Objects.equal(asA.getCountry(), asB.getCountry())) {
				return 2;
			}
		}

		return 4;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((privateNetworkId == null) ? 0 : privateNetworkId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NetworkPoint other = (NetworkPoint) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (privateNetworkId == null) {
			if (other.privateNetworkId != null) {
				return false;
			}
		} else if (!privateNetworkId.equals(other.privateNetworkId)) {
			return false;
		}
		return true;
	}

}
