package org.platformlayer.ops.firewall.simple;

import java.util.Iterator;
import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class SimpleIptablesRules implements Iterable<SimpleIptablesRule> {
	List<SimpleIptablesRule> rules = Lists.newArrayList();

	public static Command buildCommand(Transport transport, IptablesChain chain) {
		Command command;
		switch (transport) {
		case Ipv4:
			command = Command.build("iptables");
			break;
		case Ipv6:
			command = Command.build("ip6tables");
			break;
		default:
			throw new IllegalStateException();
		}

		command.addLiteral("-t").addQuoted(chain.toString().toLowerCase());
		return command;
	}

	public static SimpleIptablesRules listRules(OpsTarget target, Transport transport, IptablesChain chain)
			throws OpsException {

		Command command = buildCommand(transport, chain);
		command.addLiteral("--list-rules");

		ProcessExecution iptablesExecution = target.executeCommand(command);
		SimpleIptablesRules ret = new SimpleIptablesRules();
		for (String line : Splitter.on("\n").split(iptablesExecution.getStdOut())) {
			SimpleIptablesRule rule = new SimpleIptablesRule(line);
			ret.rules.add(rule);
		}
		return ret;
	}

	@Override
	public Iterator<SimpleIptablesRule> iterator() {
		return rules.iterator();
	}

	public SimpleIptablesRules filterByComment(String comment) {
		SimpleIptablesRules ret = new SimpleIptablesRules();

		for (SimpleIptablesRule rule : rules) {
			if (!rule.isComment(comment)) {
				continue;
			}

			ret.rules.add(rule);
		}

		return ret;
	}

	public int size() {
		return rules.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}
