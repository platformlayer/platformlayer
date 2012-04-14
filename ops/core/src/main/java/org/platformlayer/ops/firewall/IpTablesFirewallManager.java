package org.platformlayer.ops.firewall;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class IpTablesFirewallManager extends FirewallManager {
	static final Logger log = Logger.getLogger(IpTablesFirewallManager.class);

	@Override
	public void configureAddRule(OpsTarget target, FirewallRecord add) throws OpsException {
		// OpsServer server = smartGetServer(true);
		IpTablesManager.addFirewallRule(target, add);
		// getCurrentFirewallState(operation).state.add(add);
	}

	// @SupportsOperation(operation = "rebuildRules")
	// public void doRebuildRules(Operation operation) throws Exception {
	// if (!getOpsSystem().isSystemReadyAndHealthy(minimumUptime)) {
	// // log.warn("Have not yet hit minimum uptime of " + minimumUptime +
	// ", exiting (probably not all rules are configured yet, so this is unsafe at this time!)");
	// return;
	// }
	//
	// List<FirewallRecord> desired = getDesiredConfiguration();
	//
	// OpsServer server = smartGetServer(true);
	// IpTablesManager.reconfigureFirewall(server, desired);
	// removeCachedFirewallState(operation);
	// }

	public static class IpTablesFirewallState {
		public List<FirewallRecord> rules;

		public boolean hasRule(FirewallRecord rule) {
			for (FirewallRecord candidate : rules) {
				if (candidate.equals(rule)) {
					return true;
				}
			}
			return false;
		}
	}

	IpTablesFirewallState getCurrentFirewallState(OpsTarget target) throws OpsException {
		// OpsServer server = smartGetServer(true);
		// IpTablesFirewallState rules = operation.getContextData(server, IpTablesFirewallState.class);
		// if (rules.state == null) {
		// rules.state = IpTablesManager.getCurrentFirewallState(target);
		// }
		// return rules;
		IpTablesFirewallState state = new IpTablesFirewallState();
		state.rules = IpTablesManager.getCurrentFirewallState(target);
		return state;
	}

	// void removeCachedFirewallState(Operation operation) throws OpsException {
	// OpsServer server = smartGetServer(true);
	// operation.removeContextData(server, IpTablesFirewallState.class);
	// }

	@Override
	protected List<FirewallRecord> getConfiguredRules(OpsTarget target) throws OpsException {
		IpTablesFirewallState state = getCurrentFirewallState(target);
		return state.rules;
	}

	@Override
	protected void configureRemoveRule(OpsTarget target, FirewallRecord remove) throws OpsException {
		IpTablesManager.removeFirewallRule(target, remove);
		// getCurrentFirewallState(operation).state.remove(remove);
	}

	// @Override
	// protected List<FirewallRecord> getDesiredConfiguration() throws OpsException, InterruptedException {
	// List<FirewallRecord> records = Lists.newArrayList();
	//
	// // Add some IPTables specific stuff
	//
	// // Allow all outgoing, block all incoming
	// records.add(FirewallRecordParser.parseRule("block in all"));
	// records.add(FirewallRecordParser.parseRule("pass out all"));
	//
	// // # Allow all traffic on loopback.
	// records.add(FirewallRecordParser.parseRule("pass in quick on " + getLoopbackDevice() + " all"));
	// records.add(FirewallRecordParser.parseRule("pass out quick on " + getLoopbackDevice() + " all"));
	//
	// // # for testing, allow pings
	// records.add(FirewallRecordParser.parseRule("pass in quick proto icmp"));
	//
	// // # Allow outbound state related packets.
	// // configuration.addRule("pass out quick proto tcp from any to any keep state");
	// // configuration.addRule("pass out quick proto udp from any to any keep state");
	//
	// // # Allow DHCP - vital for EC2 (otherwise machines die after 48 hours)
	// records.add(FirewallRecord.pass().in().protocol(Protocol.Udp).dest(PortAddressFilter.withPortRange(67,
	// 68)).source(PortAddressFilter.withPortRange(67, 68)));
	// // .parseRule("-A INPUT -p udp --dport 67:68 --sport 67:68 -j ACCEPT"));
	//
	// // Allow keep state packets in (though we try to avoid use of these)
	// // records.add(FirewallRecordParser.parseRule("-A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT"));
	// records.add(FirewallRecord.pass().in().withKeepState());
	//
	// /*
	// * // We do allow output with keep state, but it's not marked quick. // We expect other rules to be used to avoid
	// keeping IPF state
	// * records.add(IpfManager.parseRule("pass out proto tcp from any to any keep state"));
	// records.add(IpfManager.parseRule("pass out proto udp from any to any keep state"));
	// */
	//
	// records.addAll(super.getDesiredConfiguration());
	//
	// return records;
	// }
	//
	// @Override
	// protected List<FirewallRecord> rewriteRule(OpsServer server, FirewallRecord rule) throws OpsException,
	// InterruptedException {
	// List<FirewallRecord> rewroteSrc = Lists.newArrayList();
	// rewriteVirtualAddresses(server, rule, rewroteSrc, true);
	//
	// List<FirewallRecord> rewroteDest = Lists.newArrayList();
	// for (FirewallRecord rewroteSrcRule : rewroteSrc) {
	// rewriteVirtualAddresses(server, rewroteSrcRule, rewroteDest, false);
	// }
	//
	// return rewroteDest;
	// }

}
