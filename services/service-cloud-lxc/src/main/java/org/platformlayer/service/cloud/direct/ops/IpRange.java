package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpRange {

	private final InetAddress address;
	private final int prefixLength;

	public IpRange(InetAddress address, int prefixLength) {
		this.address = address;
		this.prefixLength = prefixLength;
	}

	public static IpRange parse(String ipRange) {
		int slashIndex = ipRange.indexOf("/");
		if (slashIndex != -1) {
			InetAddress address;
			String addressString = ipRange.substring(0, slashIndex);
			try {
				address = InetAddress.getByName(addressString);
			} catch (UnknownHostException e) {
				throw new IllegalArgumentException("Error resolving: " + addressString, e);
			}
			int length = Integer.parseInt(ipRange.substring(slashIndex + 1));

			return new IpRange(address, length);
		} else {
			throw new IllegalArgumentException("Cannot parse IP range: " + ipRange);
		}
	}

	public String getNetmask() {
		// TODO: This is a lame and incomplete implementation
		switch (prefixLength) {
		case 8:
			return "255.0.0.0";
		case 16:
			return "255.255.0.0";
		case 24:
			return "255.255.255.0";

		default:
			throw new IllegalArgumentException("Netmask length not implemented: " + prefixLength);
		}
	}

	public InetAddress getAddress(int offset) {
		byte[] bytes = address.getAddress();
		for (int i = bytes.length - 1; i >= 0; i--) {
			int v = (bytes[i] & 0xff);
			v++;
			if (v >= 256) {
				v = 0;
				bytes[i] = (byte) (v & 0xff);
			} else {
				bytes[i] = (byte) (v & 0xff);

				break;
			}
		}

		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Error building address", e);
		}
	}
}
