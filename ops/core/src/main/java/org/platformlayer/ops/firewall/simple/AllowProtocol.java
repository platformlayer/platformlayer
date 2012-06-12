package org.platformlayer.ops.firewall.simple;

import org.apache.log4j.Logger;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;

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
	protected void checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) {
		for (SimpleIptablesRule rule : matches) {
			// TODO: Check 'count' of rules, to make sure not extra conditions

			if (!rule.isProtocol(protocol)) {
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
