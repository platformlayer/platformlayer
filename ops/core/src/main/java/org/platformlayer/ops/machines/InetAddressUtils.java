package org.platformlayer.ops.machines;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.platformlayer.ops.networks.IpRange;

public class InetAddressUtils {
	private static final Logger log = Logger.getLogger(InetAddressUtils.class);

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
}
