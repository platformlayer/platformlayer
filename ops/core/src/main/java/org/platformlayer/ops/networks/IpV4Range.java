package org.platformlayer.ops.networks;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpV4Range extends IpRange {
	public IpV4Range(InetAddress address, int netmaskLength) {
		super(address, netmaskLength);
	}

	private static String getNetmaskByte(int count) {
		if (count > 8) {
			count = 8;
		}
		if (count < 0) {
			count = 0;
		}

		int k = 256 - (1 << (8 - count));
		return String.valueOf(k);
	}

	public static String getNetmask(int prefixLength) {
		StringBuilder sb = new StringBuilder();
		int count = prefixLength;
		sb.append(getNetmaskByte(count));
		sb.append(".");
		count -= 8;
		sb.append(getNetmaskByte(count));
		sb.append(".");
		count -= 8;
		sb.append(getNetmaskByte(count));
		sb.append(".");
		count -= 8;
		sb.append(getNetmaskByte(count));
		return sb.toString();
	}

	@Override
	public String getNetmask() {
		return getNetmask(netmaskLength);
	}

	// public InetAddress getFirstAddress() {
	// // The first address is usually reserved, unless we've only got a /32
	// if (netmaskLength != 32) {
	// return getAddressInRange(1);
	// } else {
	// return getAddressInRange(0);
	// }
	// }

	public static IpV4Range parse(String addressString, String netmask) {
		int prefixLength = -1;

		// TODO: This is pretty inefficient...
		for (int i = 0; i < 32; i++) {
			if (getNetmask(i).equals(netmask)) {
				prefixLength = i;
				break;
			}
		}

		if (prefixLength == -1) {
			throw new IllegalArgumentException("Unknown netmask: " + netmask);
		}
		InetAddress address;
		try {
			address = InetAddress.getByName(addressString);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Error resolving: " + addressString, e);
		}

		return new IpV4Range(address, prefixLength);
	}

}
