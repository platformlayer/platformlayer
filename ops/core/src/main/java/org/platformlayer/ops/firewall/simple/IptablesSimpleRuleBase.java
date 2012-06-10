package org.platformlayer.ops.firewall.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.IptablesChain;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public abstract class IptablesSimpleRuleBase extends OpsTreeBase {
	static final Logger log = Logger.getLogger(IptablesSimpleRuleBase.class);

	public Transport transport;
	public Protocol protocol;

	public String uuid;

	@Override
	protected void addChildren() throws OpsException {

	}

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// We're trying not parsing everything (as IpTablesManager does!)

		List<Protocol> protocols = Lists.newArrayList();
		if (protocol == null) {
			protocols = Arrays.asList(Protocol.Tcp, Protocol.Udp);
		} else {
			protocols = Collections.singletonList(protocol);
		}

		List<Transport> transports = Lists.newArrayList();
		if (transport == null) {
			transports = Transport.all();
		} else {
			transports = Collections.singletonList(transport);
		}

		IptablesChain chain = getChain();

		for (Transport transport : transports) {
			SimpleIptablesRules rules = SimpleIptablesRules.listRules(target, transport, chain);

			for (Protocol protocol : protocols) {
				String comment = "pl-" + uuid + "-" + protocol.toString().toLowerCase();

				SimpleIptablesRules matches = rules.filterByComment(comment);

				if (matches.size() > 1) {
					log.warn("Found multiple matching rules: " + Joiner.on("\n").join(matches));
				}

				if (OpsContext.isConfigure()) {
					checkMatchingRules(matches, protocol);

					if (matches.isEmpty()) {
						String ruleSpec = buildRuleSpec();

						Command command = SimpleIptablesRules.buildCommand(transport, chain);
						command.addLiteral("-A").addLiteral(ruleSpec);
						command.addLiteral("-m").addLiteral("comment");
						command.addLiteral("--comment").addQuoted(comment);

						target.executeCommand(command);
					} else {
						log.info("Found existing rule: " + Joiner.on("\n").join(matches));
					}
				}

				if (OpsContext.isDelete()) {
					if (!matches.isEmpty()) {
						for (SimpleIptablesRule rule : matches) {
							log.info("Deleting rule: " + rule);
							String deleteRuleSpec = rule.convertToDeleteSpec();
							Command command = SimpleIptablesRules.buildCommand(transport, chain);
							command.addLiteral(deleteRuleSpec);
							target.executeCommand(command);
						}
					}
				}
			}
		}

	}

	protected abstract String buildRuleSpec() throws OpsException;

	protected abstract void checkMatchingRules(SimpleIptablesRules matches, Protocol protocol) throws OpsException;

	protected abstract IptablesChain getChain();
}
