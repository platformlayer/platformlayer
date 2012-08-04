package org.platformlayer.ops.firewall.scripts;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.firewall.simple.SimpleIptablesRules;

/**
 * A representation of an iptables rule, that doesn't rely on completely parsing everything
 * 
 */
public class IptablesRuleRaw implements IptablesRule {

	final String ruleSpec;
	final String normalized;
	final Transport transport;
	final IptablesChain chain;

	public IptablesRuleRaw(Transport transport, IptablesChain chain, String ruleSpec) {
		this.transport = transport;
		this.chain = chain;
		this.ruleSpec = ruleSpec;
		this.normalized = normalize(ruleSpec);
	}

	private static String normalize(String s) {
		s = s.toLowerCase();
		s = s.replace("\t", " ");
		s = s.replace("\r", " ");
		s = s.replace("\n", " ");
		while (s.contains("  ")) {
			s = s.replace("  ", " ");
		}

		return s;
	}

	@Override
	public String toString() {
		return "IptablesRuleRaw:[" + ruleSpec + "]";
	}

	public String convertToDeleteSpec() {
		String s = ruleSpec;
		if (!s.startsWith("-A ")) {
			throw new UnsupportedOperationException("Cannot convert " + s);
		}
		return "-D " + s.substring(3);
	}

	@Override
	public Transport getTransport() {
		return transport;
	}

	@Override
	public Command buildIptablesAddCommand() {
		Command command = SimpleIptablesRules.buildCommand(transport, chain);
		command.addLiteral(ruleSpec);
		return command;
	}

	@Override
	public Command buildIptablesDeleteCommand() {
		Command command = SimpleIptablesRules.buildCommand(transport, chain);
		command.addLiteral(convertToDeleteSpec());
		return command;
	}

}
