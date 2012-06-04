package org.platformlayer.service.cloud.direct.ops;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.FirewallRecord.Transport;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class ForwardPort {
	static final Logger log = Logger.getLogger(ForwardPort.class);

	public Provider<InetSocketAddress> publicAddress;
	public OpsProvider<String> privateAddress;
	public int privatePort;

	public String uuid;

	public Transport transport;

	/**
	 * A representation of an iptables rule, that doesn't rely on completely parsing everything
	 * 
	 */
	static class SimpleIptablesRule {

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

	}

	static enum Protocol {
		Tcp, Udp
	}

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// We're trying not parsing everything (as IpTablesManager does!)

		ProcessExecution iptablesExecution = target.executeCommand(Command.build("iptables --list-rules -t nat"));

		List<SimpleIptablesRule> rules = Lists.newArrayList();
		for (String line : Splitter.on("\n").split(iptablesExecution.getStdOut())) {
			SimpleIptablesRule rule = new SimpleIptablesRule(line);
			rules.add(rule);
		}

		for (Protocol protocol : Arrays.asList(Protocol.Tcp, Protocol.Udp)) {
			List<SimpleIptablesRule> matches = Lists.newArrayList();

			String comment = "pl-" + uuid + "-" + protocol.toString().toLowerCase();

			for (SimpleIptablesRule rule : rules) {
				if (!rule.isComment(comment)) {
					continue;
				}

				matches.add(rule);
			}

			if (matches.size() > 1) {
				log.warn("Found multiple matching rules: " + Joiner.on("\n").join(matches));
			}

			if (OpsContext.isConfigure()) {
				String dest = privateAddress.get() + ":" + privatePort;
				InetSocketAddress publicSocketAddress = this.publicAddress.get();

				for (SimpleIptablesRule rule : matches) {
					if (!rule.isPrerouting()) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}

					if (!rule.isDnat()) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}

					if (!rule.isProtocol(protocol)) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}

					if (!rule.isMatchPort(publicSocketAddress.getPort())) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}
					if (!rule.isMatchAddress(publicSocketAddress.getAddress())) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}

					if (!rule.isDnatDestination(dest)) {
						log.warn("Found matching comment, but rule did not match: " + rule);
						continue;
					}
				}

				if (matches.isEmpty()) {
					String ruleSpec = "PREROUTING";
					ruleSpec += " --dst " + publicSocketAddress.getAddress().getHostAddress();
					ruleSpec += " -p " + protocol;
					ruleSpec += " --dport " + publicSocketAddress.getPort();
					ruleSpec += " -j DNAT";
					ruleSpec += " --to " + dest;

					Command command = Command.build("iptables -t nat -A " + ruleSpec);
					command.addLiteral("-m").addLiteral("comment");
					command.addLiteral("--comment").addQuoted(comment);

					target.executeCommand(command);
				} else {
					log.info("Found existing rule: " + Joiner.on("\n").join(matches));
				}
			}

			if (OpsContext.isDelete()) {
				if (!matches.isEmpty()) {
					for (SimpleIptablesRule rule : matches) {
						log.info("Deleting rule: " + rule);
						String deleteRuleSpec = rule.convertToDeleteSpec();
						Command command = Command.build("iptables -t nat " + deleteRuleSpec);
						target.executeCommand(command);
					}
				}
			}
		}
	}
}
