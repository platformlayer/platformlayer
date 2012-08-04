package org.platformlayer.ops.firewall;

import java.util.Arrays;
import java.util.List;

import org.platformlayer.EnumUtils;

public enum Protocol {
	All, Tcp, Udp, Icmp, TcpOrUdp, Esp, Ah;

	public static Protocol parse(String proto) {
		if (proto.equalsIgnoreCase("tcp/udp")) {
			return TcpOrUdp;
		}
		return EnumUtils.valueOfCaseInsensitive(Protocol.class, proto);
	}

	public static List<Protocol> TcpAndUdp() {
		return Arrays.asList(Protocol.Tcp, Protocol.Udp);
	}

	public String toIpfString() {
		switch (this) {
		case TcpOrUdp:
			return "tcp/udp";

		default:
			return this.toString().toLowerCase();
		}
	}
}
