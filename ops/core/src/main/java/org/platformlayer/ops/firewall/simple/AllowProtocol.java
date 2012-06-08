package org.platformlayer.ops.firewall.simple;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.Protocol;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class AllowProtocol {
	static final Logger log = Logger.getLogger(AllowProtocol.class);

	public Protocol protocol;
	public String uuid;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// We're trying not parsing everything (as IpTablesManager does!)

		List<SimpleIptablesRule> rules = SimpleIptablesRule.listRules(target, "filter");

		List<SimpleIptablesRule> matches = Lists.newArrayList();

		String comment = "pl-" + uuid;

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
			for (SimpleIptablesRule rule : matches) {
				// TODO: Check 'count' of rules, to make sure not extra conditions

				if (!rule.isProtocol(protocol)) {
					log.warn("Found matching comment, but rule did not match: " + rule);
					continue;
				}
			}

			if (matches.isEmpty()) {
				String ruleSpec = "INPUT";
				ruleSpec += " -p " + protocol;
				ruleSpec += " -j ACCEPT";

				Command command = Command.build("iptables -t filter -A " + ruleSpec);
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
					Command command = Command.build("iptables -t filter " + deleteRuleSpec);
					target.executeCommand(command);
				}
			}
		}
	}
}
