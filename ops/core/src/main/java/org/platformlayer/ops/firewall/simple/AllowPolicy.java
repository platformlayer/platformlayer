package org.platformlayer.ops.firewall.simple;

import org.apache.log4j.Logger;
import org.platformlayer.ops.firewall.FirewallRecord.Direction;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;

public class AllowPolicy extends IptablesSimpleRuleBase {
	static final Logger log = Logger.getLogger(ForwardPort.class);

	public String policy;
	public Direction direction;

	@Override
	protected String buildRuleSpec(Protocol protocol) {
		String ruleSpec = "INPUT";
		ruleSpec += " --match policy ";
		ruleSpec += " --pol " + policy;
		ruleSpec += " --dir " + direction.toString().toLowerCase();
		ruleSpec += " -j ACCEPT";

		return ruleSpec;
	}

	@Override
	protected void checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) {
		for (SimpleIptablesRule rule : matches) {
			if (!rule.isInput()) {
				log.warn("Found matching comment, but rule did not match: " + rule);
				continue;
			}

			if (!rule.isMatchPolicy()) {
				log.warn("Found matching comment, but rule did not match: " + rule);
				continue;
			}

			if (!rule.isPolicy(policy)) {
				log.warn("Found matching comment, but rule did not match: " + rule);
				continue;
			}

			if (!rule.isDirection(direction)) {
				log.warn("Found matching comment, but rule did not match: " + rule);
				continue;
			}
		}
	}

	@Override
	protected IptablesChain getChain() {
		return IptablesChain.Filter;
	}
}
