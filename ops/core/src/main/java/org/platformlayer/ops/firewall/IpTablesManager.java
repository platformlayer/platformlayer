package org.platformlayer.ops.firewall;

import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.FirewallRecord.Decision;
import org.platformlayer.ops.firewall.FirewallRecord.Direction;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.collect.Lists;

public class IpTablesManager {
	static final Logger log = Logger.getLogger(IpTablesManager.class);

	// static final String CMD_IPFSTAT = "/usr/sbin/ipfstat ";
	static final String CMD_IPTABLES4 = "/sbin/iptables ";
	static final String CMD_IPTABLES6 = "/sbin/ip6tables ";

	static Command getIptablesCommand(Transport transport) {
		switch (transport) {
		case Ipv4:
			return Command.build(CMD_IPTABLES4);
		case Ipv6:
			return Command.build(CMD_IPTABLES6);
		default:
			throw new IllegalArgumentException();
		}
	}

	public static List<FirewallRecord> getCurrentFirewallState(OpsTarget target, Transport transport)
			throws OpsException {
		Command command = getIptablesCommand(transport);
		command.addLiteral("--list-rules");

		List<FirewallRecord> records = Lists.newArrayList();

		ProcessExecution execution = target.executeCommand(command);
		String stdout = execution.getStdOut();
		for (String line : stdout.split("\n")) {
			parseAndAdd(records, line);
		}
		return records;
	}

	private static void parseAndAdd(List<FirewallRecord> records, String line) {
		try {
			FirewallRecord record = parseRule(line);
			if (record != null) {
				records.add(record);
			}
		} catch (Exception e) {
			log.error("Error parsing line " + line, e);
		}
	}

	public static FirewallRecord parseRule(String rule) {
		String[] tokenArray = rule.split(" ");
		if (tokenArray.length < 3) {
			log.info("Cannot parse rule: " + rule);
			return null;
		}

		Queue<String> tokens = Lists.newLinkedList();
		for (String token : tokenArray) {
			tokens.add(token);
		}

		if (tokens.peek().equals("-P")) {
			return parseDefaultRule(rule, tokens);
		}

		FirewallRecord record = new FirewallRecord();
		record.setQuick(true);

		// -A INPUT -s 74.125.67.99/32 -d 209.191.93.52/32 -i lo0 -p tcp -m tcp --sport 33 --dport 22 -j ACCEPT

		// record.decision = EnumUtils.valueOfCaseInsensitive(FirewallRecord.Decision.class, tokens.remove());
		// record.direction = EnumUtils.valueOfCaseInsensitive(FirewallRecord.Direction.class, tokens.remove());

		while (!tokens.isEmpty()) {
			String nextToken = tokens.remove();
			if (nextToken.equals("-A")) {
				// Chain spec
				String chain = tokens.remove();
				if (chain.equals("INPUT")) {
					record.direction = Direction.In;
				} else if (chain.equals("OUTPUT")) {
					record.direction = Direction.Out;
				} else {
					throw new IllegalArgumentException("Cannot parse -A chain in " + rule);
				}
			} else if (nextToken.equals("-j")) {
				String chain = tokens.remove();
				if (chain.equals("ACCEPT")) {
					record.decision = Decision.Pass;
				} else if (chain.equals("DROP")) {
					record.decision = Decision.Block;
				} else {
					throw new IllegalArgumentException("Cannot parse -j decision in " + rule);
				}
			} else if (nextToken.equals("-p")) {
				String proto = tokens.remove();
				if (proto.equals("tcp")) {
					record.protocol = Protocol.Tcp;
				} else if (proto.equals("udp")) {
					record.protocol = Protocol.Udp;
				} else if (proto.equals("icmp")) {
					record.protocol = Protocol.Icmp;
				} else if (proto.equals("esp")) {
					record.protocol = Protocol.Esp;
				} else if (proto.equals("ah")) {
					record.protocol = Protocol.Ah;
				} else {
					throw new IllegalArgumentException("Cannot parse -p protocol in " + rule);
				}
			} else if (nextToken.equals("-m")) {
				String module = tokens.remove();
				if (module.equals("tcp") || module.equals("udp") || module.equals("icmp")) {
					// We deal with this in the protocol spec
				} else if (module.equals("policy")) {
					String policy = null;
					String direction = null;

					// Used to allow in packets that went through IPSEC
					// e.g. iptables -A INPUT -m policy --pol ipsec --dir in -j ACCEPT
					while (!tokens.isEmpty()) {
						String peekToken = tokens.peek();
						if (peekToken.equals("--pol")) {
							tokens.remove();
							policy = tokens.remove();
						} else if (peekToken.equals("--dir")) {
							tokens.remove();
							direction = tokens.remove();
						} else {
							break;
						}
					}

					if ("ipsec".equals(policy)) {
						if ("in".equals(direction)) {
							if (record.direction != Direction.In) {
								throw new IllegalStateException("Direction mismatch");
							}
						} else if ("out".equals(direction)) {
							if (record.direction != Direction.Out) {
								throw new IllegalStateException("Direction mismatch");
							}
						} else {
							throw new IllegalStateException("Unexpected direction");
						}

						record.fromIpsec = true;
					} else {
						throw new IllegalStateException("Only IPSEC policy is supported");
					}
				} else if (module.equals("state")) {
					if (!tokens.remove().equals("--state")) {
						throw new IllegalStateException("Expected --state");
					}
					String stateRule = tokens.remove();
					switch (record.direction) {
					case In:
						if (!stateRule.equals("ESTABLISHED") && !stateRule.equals("RELATED,ESTABLISHED")
								&& !stateRule.equals("ESTABLISHED,RELATED")) {
							throw new IllegalStateException("Expected --state ESTABLISHED or RELATED,ESTABLISHED, was "
									+ stateRule);
						}
						break;

					case Out:
						if (!stateRule.equals("NEW")) {
							throw new IllegalStateException("Expected --state NEW, was " + stateRule);
						}
						break;

					default:
						throw new IllegalStateException();
					}
					record.keepState = true;
				} else {
					throw new IllegalArgumentException("Cannot parse -m module in " + rule);
				}
			} else if (nextToken.equals("-i")) {
				String device = tokens.remove();
				record.device = device;
			} else if (nextToken.equals("-o")) {
				String device = tokens.remove();
				record.device = device;
			} else if (nextToken.equals("-s")) {
				String netmask = tokens.remove();
				record.getSrcFilter().setNetmask(parseNetmask(netmask));
			} else if (nextToken.equals("-d")) {
				String netmask = tokens.remove();
				record.getDestFilter().setNetmask(parseNetmask(netmask));
			} else if (nextToken.equals("--sport")) {
				String portSpec = tokens.remove();
				parsePorts(portSpec, record.getSrcFilter());
			} else if (nextToken.equals("--dport")) {
				String portSpec = tokens.remove();
				parsePorts(portSpec, record.getDestFilter());
			} else if (nextToken.equals("--icmp-type")) {
				String icmpType = tokens.remove();
				if (icmpType.equals("any")) {
					// OK
				} else {
					throw new IllegalArgumentException("Cannot parse ---icmp-type in " + rule);
				}
			} else {
				throw new IllegalArgumentException("Unknown token in rule: " + rule + " token=" + nextToken);
			}
		}

		if (record.decision == null || record.direction == null) {
			throw new IllegalArgumentException("Invalid rule (post-parse checks failed): " + rule);
		}

		return record;
	}

	private static FirewallRecord parseDefaultRule(String rule, Queue<String> tokens) {
		FirewallRecord record = new FirewallRecord();

		while (!tokens.isEmpty()) {
			String nextToken = tokens.remove();

			if (nextToken.equals("-P")) {
				// Default policy
				String chain = tokens.remove();
				if (chain.equals("INPUT")) {
					record.direction = Direction.In;
				} else if (chain.equals("OUTPUT")) {
					record.direction = Direction.Out;
				} else if (chain.equals("FORWARD")) {
					// Ignore
					return null;
				} else {
					throw new IllegalArgumentException("Cannot parse -P chain in " + rule);
				}

				String policy = tokens.remove();
				if (policy.equals("ACCEPT")) {
					record.decision = Decision.Pass;
				} else if (policy.equals("DROP")) {
					record.decision = Decision.Block;
				} else {
					throw new IllegalArgumentException("Cannot parse -P decision in " + rule);
				}
			} else {
				throw new IllegalArgumentException("Unknown token in rule: " + rule + " token=" + nextToken);
			}
		}

		return record;
	}

	private static void parsePorts(String portSpec, PortAddressFilter filter) {
		String[] tokens = portSpec.split(":");
		if (tokens.length == 1) {
			int port = Integer.parseInt(portSpec);
			filter.setPortHigh(port);
			filter.setPortLow(port);
		} else if (tokens.length == 2) {
			filter.setPortLow(Integer.parseInt(tokens[0]));
			filter.setPortHigh(Integer.parseInt(tokens[1]));
		} else {
			throw new IllegalStateException("Cannot parse port spec: " + portSpec);
		}
	}

	private static FirewallNetmask parseNetmask(String token) {
		// if (token.equals("any")) {
		// return FirewallNetmask.Public;
		// }
		String cidr = token;
		return FirewallNetmask.buildCidr(cidr);
	}

	public static Command buildCommandAddFirewallRule(OpsTarget server, FirewallRecord add) throws OpsException {
		if (isPolicyDefault(add)) {
			Command command = getIptablesCommand(add.getTransport());
			command.addLiteral("-P");
			command.addLiteral(toChain(add));
			command.addLiteral(toIpTableDecision(add.decision));
			return command;
		}

		// iptables --append INPUT -s 74.125.67.103/32 -p tcp -m tcp --dport 22 -j ACCEPT
		String action;
		// Passes take precedence
		switch (add.decision) {
		case Pass:
			action = " --insert " + toChain(add) + " 1 ";
			break;
		case Block:
			action = " --append " + toChain(add);
			break;
		default:
			throw new IllegalStateException();
		}
		String ipTableRule = buildIpTableRule(add);
		String commandString = getIptablesCommand(add.getTransport()).buildCommandString() + action + ipTableRule;
		Command command = Command.build(commandString);
		return command;
	}

	private static boolean isPolicyDefault(FirewallRecord record) {
		return !record.isQuick() && record.getDestFilter().isUnfiltered() && record.getSrcFilter().isUnfiltered()
				&& record.getDevice() == null && record.getProtocol() == Protocol.All;
	}

	// public static void reconfigureFirewall(OpsServer server, List<FirewallRecord> allRules) throws OpsException {
	// throw new UnsupportedOperationException();
	// // StringBuilder ipfConfig = new StringBuilder();
	// // for (FirewallRecord record : allRules) {
	// // List<String> ipfRules = buildIpfRules(server, record);
	// // for (String ipfRule : ipfRules) {
	// // ipfConfig.append(ipfRule);
	// // ipfConfig.append('\n');
	// // }
	// // }
	// //
	// // Agent agent = server.getAgent();
	// // String configFile = agent.uploadTempTextFile(ipfConfig.toString(), "0400", "root");
	// // try {
	// // String commandClearAllInactive = CMD_IPF + " -I -Fa";
	// // server.simpleRun(commandClearAllInactive);
	// //
	// // SimpleBashCommand loadRules = SimpleBashCommand.build(CMD_IPF);
	// // loadRules.addLiteralArg("-I");
	// // loadRules.addLiteralArg("-f");
	// // loadRules.addFileArg(configFile);
	// // server.simpleRun(loadRules);
	// //
	// // String commandSwitchRules = CMD_IPF + " -s";
	// // server.simpleRun(commandSwitchRules);
	// // } finally {
	// // agent.rm(configFile);
	// // }
	// }

	public static void removeFirewallRule(OpsTarget server, FirewallRecord remove) throws OpsException {
		if (isPolicyDefault(remove)) {
			log.info("Ignoring removing of policy default rule: " + remove);
			return;
		}

		// iptables --delete INPUT -s 74.125.67.103/32 -p tcp -m tcp --dport 22 -j ACCEP
		String ipTableRule = buildIpTableRule(remove);
		String commandString = getIptablesCommand(remove.getTransport()).buildCommandString() + " --delete "
				+ toChain(remove) + " " + ipTableRule;
		Command command = Command.build(commandString);
		server.executeCommand(command);
	}

	private static String toChain(FirewallRecord record) {
		switch (record.direction) {
		case In:
			return "INPUT";
		case Out:
			return "OUTPUT";

		default:
			throw new IllegalArgumentException();
		}
	}

	private static String buildIpTableRule(FirewallRecord record) {
		// -s 192.168.1.10 -d 10.1.15.1 -p tcp --dport 22 -j ACCEPT
		StringBuilder ipTableRule = new StringBuilder();

		// -s 1.2.3.4/32
		if (!record.getSrcFilter().getNetmask().isUnfiltered()) {
			ipTableRule.append("-s ");
			ipTableRule.append(toIpTablesNetmask(record.getSrcFilter().getNetmask()));
		}

		// -d 4.3.2.1/32
		if (!record.getDestFilter().getNetmask().isUnfiltered()) {
			ipTableRule.append(" -d ");
			ipTableRule.append(toIpTablesNetmask(record.getDestFilter().getNetmask()));
		}

		if (record.device != null) {
			switch (record.direction) {
			case In:
				ipTableRule.append(" -i ");
				break;

			case Out:
				ipTableRule.append(" -o ");
				break;

			default:
				throw new IllegalStateException();
			}

			ipTableRule.append(record.device);
		}

		// -p tcp -m tcp
		if (record.protocol != Protocol.All) {
			switch (record.protocol) {
			case Tcp:
				ipTableRule.append(" -p tcp -m tcp");
				break;
			case Icmp:
				ipTableRule.append(" -p icmp -m icmp");
				break;
			case Udp:
				ipTableRule.append(" -p udp -m udp");
				break;
			case Esp:
				ipTableRule.append(" -p esp");
				break;
			case Ah:
				ipTableRule.append(" -p ah");
				break;
			default:
				throw new IllegalArgumentException("Unhandled protocol: " + record.protocol);
			}
		}

		// --sport 33
		String sourcePort = buildPortRule(record.getSrcFilter());
		if (sourcePort != null) {
			ipTableRule.append(" --sport ");
			ipTableRule.append(sourcePort);
		}

		// --dport 33
		String destPort = buildPortRule(record.getDestFilter());
		if (destPort != null) {
			ipTableRule.append(" --dport ");
			ipTableRule.append(destPort);
		}

		if (record.fromIpsec) {
			ipTableRule.append(" -m policy --pol ipsec --dir " + record.direction.toString().toLowerCase());
		}

		if (record.keepState) {
			switch (record.direction) {
			case In:
				ipTableRule.append(" -m state --state RELATED,ESTABLISHED");
				break;

			case Out:
				ipTableRule.append(" -m state --state NEW");
				break;

			default:
				throw new IllegalStateException();
			}
		}

		ipTableRule.append(" -j ");
		ipTableRule.append(toIpTableDecision(record.decision));

		if (!record.isQuick) {
			throw new IllegalArgumentException("Non-quick not implemented for IpTables");
		}

		return ipTableRule.toString();
	}

	private static String toIpTableDecision(Decision decision) {
		switch (decision) {
		case Block:
			return "DROP";
		case Pass:
			return "ACCEPT";
		default:
			throw new IllegalStateException();
		}
	}

	private static String buildPortRule(PortAddressFilter filter) {
		String rule = null;

		if (filter.getPortLow() != 0) {
			rule = "";
			// --source-port or --destination-port
			if (filter.getPortHigh() == filter.getPortLow()) {
				rule += Integer.toString(filter.getPortHigh());
			} else {
				rule += Integer.toString(filter.getPortLow());
				rule += ":";
				rule += Integer.toString(filter.getPortHigh());
			}
		}

		return rule;
	}

	private static String toIpTablesNetmask(FirewallNetmask firewallNetmask) {
		switch (firewallNetmask.getNetmaskType()) {
		case Cidr:
			return firewallNetmask.buildCidr();

		default:
			throw new IllegalArgumentException("Unhandled type: " + firewallNetmask.getNetmaskType());
		}
	}

}