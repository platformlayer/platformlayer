package org.platformlayer.ops.firewall.scripts;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class IptablesFilterEntry extends IpTablesRuleScript {
	static final Logger log = LoggerFactory.getLogger(IptablesFilterEntry.class);

	// Inherited:
	// public String ruleKey;
	// public String interfaceName = "eth0";

	public Transport transport;
	public Protocol protocol = Protocol.Tcp;
	public int port;
	public String sourceCidr;

	@Override
	protected IptablesRule getRule() {
		String ruleSpec = "-A INPUT";

		ruleSpec += " -p " + protocol;

		if (!Strings.isNullOrEmpty(sourceCidr)) {
			ruleSpec += " -s " + sourceCidr;
		}

		if (port != 0) {
			ruleSpec += " --dport " + port;
		}

		ruleSpec += " -j ACCEPT";

		return new IptablesRuleRaw(transport, IptablesChain.Filter, ruleSpec);
	}

	@Override
	protected Transport getRuleTransport() throws OpsException {
		return transport;
	}

}
