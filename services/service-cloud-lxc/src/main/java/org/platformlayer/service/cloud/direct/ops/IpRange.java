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

	public String getNetmask() {
		return getNetmask(prefixLength);
	}

	public InetAddress getAddress(int offset) {
		byte[] bytes = address.getAddress();
		if (offset == 0) {

		} else if (offset == 1) {
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
		} else {
			// TODO: I don't know why I didn't implement this!
			throw new UnsupportedOperationException();
		}

		try {
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Error building address", e);
		}
	}

	public InetAddress getFirstAddress() {
		// The first address is usually reserved, unless we've only got a /32
		if (prefixLength != 32) {
			return getAddress(1);
		} else {
			return getAddress(0);
		}
	}

	public static IpRange parse(String addressString, String netmask) {
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

		return new IpRange(address, prefixLength);
	}

	public int getPrefixLength() {
		return prefixLength;
	}
}
