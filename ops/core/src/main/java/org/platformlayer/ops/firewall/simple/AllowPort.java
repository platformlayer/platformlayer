package org.platformlayer.ops.firewall.simple;

import org.apache.log4j.Logger;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;

/*
 * An experimental port-opening version that just opens the iptables port.
 * TODO: Rationalize with our full port support; we probably want to replace that
 * iptables implementation with this one
 */
public class AllowPort extends IptablesSimpleRuleBase {
	static final Logger log = Logger.getLogger(ForwardPort.class);

	public int port;

	@Override
	protected String buildRuleSpec() {
		String ruleSpec = "INPUT";

		ruleSpec += " -p " + protocol;
		ruleSpec += " --dport " + port;
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

			if (!rule.isProtocol(protocol)) {
				log.warn("Found matching comment, but rule did not match: " + rule);
				continue;
			}

			if (!rule.isMatchPort(port)) {
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
