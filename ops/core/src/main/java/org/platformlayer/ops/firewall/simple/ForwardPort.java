package org.platformlayer.ops.firewall.simple;

import java.net.InetSocketAddress;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;

public class ForwardPort extends IptablesSimpleRuleBase {
	static final Logger log = Logger.getLogger(ForwardPort.class);

	public Provider<InetSocketAddress> publicAddress;
	public OpsProvider<String> privateAddress;
	public int privatePort;

	public ForwardPort() {
		// IPV6 doesn't really need forwarding
		transport = Transport.Ipv4;
	}

	@Override
	protected void checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) throws OpsException {
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

	}

	@Override
	protected IptablesChain getChain() {
		return IptablesChain.Nat;
	}

	@Override
	protected String buildRuleSpec() throws OpsException {
		String dest = privateAddress.get() + ":" + privatePort;
		InetSocketAddress publicSocketAddress = this.publicAddress.get();

		String ruleSpec = "PREROUTING";
		ruleSpec += " --dst " + publicSocketAddress.getAddress().getHostAddress();
		ruleSpec += " -p " + protocol;
		ruleSpec += " --dport " + publicSocketAddress.getPort();
		ruleSpec += " -j DNAT";
		ruleSpec += " --to " + dest;

		return ruleSpec;
	}
}
