package org.platformlayer.ops.firewall;

import java.util.Arrays;
import java.util.List;

import org.platformlayer.ops.EnumUtils;

public enum Transport {
	// TODO: Is transport the right word?
	Ipv4, Ipv6;

	public static List<Transport> all() {
		return Arrays.asList(Transport.values());
	}

	public static Transport parse(String value) {
		if (value == null) {
			return null;
		}

		return EnumUtils.valueOfCaseInsensitive(Transport.class, value);
	}

	public String getKey() {
		return toString().toLowerCase();
	}
}
