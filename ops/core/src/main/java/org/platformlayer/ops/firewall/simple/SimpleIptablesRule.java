package org.platformlayer.ops.firewall.simple;

import java.net.InetAddress;
import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

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

	public static List<SimpleIptablesRule> listRules(OpsTarget target, String chain) throws OpsException {
		ProcessExecution iptablesExecution = target
				.executeCommand(Command.build("iptables --list-rules -t {0}", chain));
		List<SimpleIptablesRule> rules = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(iptablesExecution.getStdOut())) {
			SimpleIptablesRule rule = new SimpleIptablesRule(line);
			rules.add(rule);
		}
		return rules;
	}

}
