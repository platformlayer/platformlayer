package org.platformlayer.ops.firewall.scripts;

import java.net.InetSocketAddress;

import javax.inject.Provider;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IptablesForwardPort extends IpTablesRuleScript {
	static final Logger log = LoggerFactory.getLogger(IptablesForwardPort.class);

	public Provider<InetSocketAddress> publicAddress;
	public OpsProvider<String> privateAddress;
	public int privatePort;
	public Protocol protocol = Protocol.Tcp;

	// IPV6 doesn't really need forwarding
	final Transport transport = Transport.Ipv4;

	@Override
	protected IptablesRule getRule() throws OpsException {
		String dest = privateAddress.get() + ":" + privatePort;
		InetSocketAddress publicSocketAddress = this.publicAddress.get();

		String ruleSpec = "-A PREROUTING";
		ruleSpec += " --dst " + publicSocketAddress.getAddress().getHostAddress();
		ruleSpec += " -p " + protocol;
		ruleSpec += " --dport " + publicSocketAddress.getPort();
		ruleSpec += " -j DNAT";
		ruleSpec += " --to " + dest;

		return new IptablesRuleRaw(transport, IptablesChain.Nat, ruleSpec);
	}

	@Override
	protected Transport getRuleTransport() throws OpsException {
		return transport;
	}
}
