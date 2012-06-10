package org.platformlayer.ops.firewall.simple;

import java.net.InetAddress;

import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.machines.InetAddressUtils;

/**
 * A representation of an iptables rule, that doesn't rely on completely parsing everything
 * 
 */
public class SimpleIptablesRule {

	private final String line;
	final String normalized;

	public SimpleIptablesRule(String line) {
		this.line = line;
		this.normalized = normalize(line);
	}

	private static String normalize(String s) {
		s = s.toLowerCase();
		s = s.replace("\t", " ");
		s = s.replace("\r", " ");
		s = s.replace("\n", " ");
		while (s.contains("  ")) {
			s = s.replace("  ", " ");
		}

		return s;
	}

	public boolean isPrerouting() {
		return normalized.contains("-a prerouting");
	}

	public boolean isInput() {
		return normalized.contains("-a input");
	}

	public boolean isDnat() {
		return normalized.contains("-j dnat");
	}

	public boolean isComment(String comment) {
		return normalized.contains("--comment " + comment);
	}

	public boolean isProtocol(Protocol protocol) {
		return normalized.contains("-p " + protocol.toString().toLowerCase());
	}

	public boolean isMatchPort(int publicPort) {
		return normalized.contains("--dport " + publicPort);
	}

	public boolean isMatchAddress(String cidr) {
		return normalized.contains("-d " + cidr.toLowerCase());
	}

	public boolean isMatchAddress(InetAddress address) {
		if (InetAddressUtils.isIpv4(address)) {
			return isMatchAddress(address.getHostAddress() + "/32");
		} else {
			throw new UnsupportedOperationException();
			// return isMatchAddress(address.getHostAddress() );
		}
	}

	public boolean isDnatDestination(String dest) {
		return normalized.contains("--to-destination " + dest.toLowerCase());
	}

	@Override
	public String toString() {
		return "SimpleIptablesRule:[" + line + "]";
	}

	public String convertToDeleteSpec() {
		String s = line;
		if (!s.startsWith("-A ")) {
			throw new UnsupportedOperationException("Cannot convert " + s);
		}
		return "-D " + s.substring(3);
	}

}
