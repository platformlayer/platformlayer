package org.platformlayer.ops.firewall.simple;

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
import org.platformlayer.ops.firewall.Protocol;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ForwardPort {
	static final Logger log = Logger.getLogger(ForwardPort.class);

	public Provider<InetSocketAddress> publicAddress;
	public OpsProvider<String> privateAddress;
	public int privatePort;

	public String uuid;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// We're trying not parsing everything (as IpTablesManager does!)

		List<SimpleIptablesRule> rules = SimpleIptablesRule.listRules(target, "nat");

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
