package org.platformlayer.ops.machines;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.networks.IpRange;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class InetAddressUtils {
	private static final Logger log = Logger.getLogger(InetAddressUtils.class);

	public static final Predicate<? super InetAddress> IS_IPV6 = new Predicate<InetAddress>() {
		@Override
		public boolean apply(InetAddress address) {
			return isIpv6(address);
		}
	};

	public static final Predicate<? super InetAddress> IS_IPV4 = new Predicate<InetAddress>() {
		@Override
		public boolean apply(InetAddress address) {
			return isIpv4(address);
		}
	};

	public static boolean isPublic(InetAddress address) {
		if (isIpv6(address)) {
			return true;
		} else {
			IpRange private10 = IpRange.parse("10.0.0.0/8");
			IpRange private17216 = IpRange.parse("172.16.0.0/12");
			IpRange private192168 = IpRange.parse("192.168.0.0/16");

			if (private10.isInRange(address)) {
				return false;
			}
			if (private17216.isInRange(address)) {
				return false;
			}
			if (private192168.isInRange(address)) {
				return false;
			}
			return true;
		}
	}

	public static boolean isIpv6(InetAddress address) {
		return address instanceof Inet6Address;
	}

	public static boolean isIpv4(InetAddress address) {
		return address instanceof Inet4Address;
	}

	public static List<InetAddress> getLocalAddresses() {
		List<InetAddress> addresses = Lists.newArrayList();
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new IllegalStateException("Error reading network addresses", e);
		}

		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
			for (InterfaceAddress interfaceAddress : interfaceAddresses) {
				InetAddress address = interfaceAddress.getAddress();
				addresses.add(address);
			}
		}

		return addresses;
	}
}
