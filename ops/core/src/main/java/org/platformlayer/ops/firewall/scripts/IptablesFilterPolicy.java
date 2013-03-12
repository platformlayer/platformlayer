package org.platformlayer.ops.firewall.scripts;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.FirewallRecord.Direction;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IptablesFilterPolicy extends IpTablesRuleScript {
	static final Logger log = LoggerFactory.getLogger(IptablesFilterPolicy.class);

	// Inherited:
	// public String ruleKey;
	// public String interfaceName = "eth0";

	public String policy;
	public Direction direction;
	public Transport transport;

	@Override
	protected IptablesRule getRule() {
		String ruleSpec = "-A INPUT";
		ruleSpec += " --match policy ";
		ruleSpec += " --pol " + policy;
		ruleSpec += " --dir " + direction.toString().toLowerCase();
		ruleSpec += " -j ACCEPT";

		return new IptablesRuleRaw(transport, IptablesChain.Filter, ruleSpec);
	}

	@Override
	protected Transport getRuleTransport() throws OpsException {
		return transport;
	}

}
