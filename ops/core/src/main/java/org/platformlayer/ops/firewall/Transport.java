package org.platformlayer.ops.firewall;

import java.util.Arrays;
import java.util.List;

public enum Transport {
	// TODO: Is transport the right word?
	Ipv4, Ipv6;

	public static List<Transport> all() {
		return Arrays.asList(Transport.values());
	}
}
