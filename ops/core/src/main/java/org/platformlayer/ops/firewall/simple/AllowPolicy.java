package org.platformlayer.ops.firewall.simple;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.firewall.FirewallRecord.Direction;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;

import com.google.common.collect.Lists;

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
	protected List<SimpleIptablesRule> checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) {
		List<SimpleIptablesRule> correct = Lists.newArrayList();
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

			correct.add(rule);
		}
		return correct;
	}

	@Override
	protected IptablesChain getChain() {
		return IptablesChain.Filter;
	}
}
