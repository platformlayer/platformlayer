package org.platformlayer.ops.machines;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkAddress {

	private final InetAddress inetAddress;

	public NetworkAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
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
