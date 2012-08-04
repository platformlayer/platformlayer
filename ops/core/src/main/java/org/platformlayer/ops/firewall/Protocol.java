package org.platformlayer.ops.firewall;

import org.platformlayer.EnumUtils;

public enum Protocol {
	All, Tcp, Udp, Icmp, TcpOrUdp, Esp, Ah;

	public static Protocol parse(String proto) {
		if (proto.equalsIgnoreCase("tcp/udp")) {
			return TcpOrUdp;
		}
		return EnumUtils.valueOfCaseInsensitive(Protocol.class, proto);
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
