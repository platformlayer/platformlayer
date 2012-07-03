package org.platformlayer.ops.firewall.simple;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;

import com.google.common.collect.Lists;

public class AllowProtocol extends IptablesSimpleRuleBase {
	static final Logger log = Logger.getLogger(AllowProtocol.class);

	@Override
	protected String buildRuleSpec(Protocol protocol) {
		String ruleSpec = "INPUT";
		ruleSpec += " -p " + protocol;
		ruleSpec += " -j ACCEPT";

		return ruleSpec;
	}

	@Override
	protected List<SimpleIptablesRule> checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) {
		List<SimpleIptablesRule> correct = Lists.newArrayList();
		for (SimpleIptablesRule rule : matches) {
			// TODO: Check 'count' of rules, to make sure not extra conditions

			if (!rule.isProtocol(protocol)) {
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
