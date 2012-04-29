package org.platformlayer.ops.firewall;

import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.firewall.IpTablesFirewallManager.IpTablesFirewallState;

import com.google.inject.util.Providers;

//@Icon("firewall")
public class FirewallEntry {
	static final Logger log = Logger.getLogger(FirewallEntry.class);

	public Provider<FirewallRecord> rule;

	public static FirewallEntry build(FirewallRecord rule) {
		return build(Providers.of(rule));
	}

	public static FirewallEntry build(Provider<FirewallRecord> rule) {
		FirewallEntry entry = OpsContext.get().getInjector().getInstance(FirewallEntry.class);
		entry.rule = rule;
		return entry;
	}

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// OpsServer server = firewallEntry.smartGetServer();
		// if (server == null)
		// return;
		//
		// if (!firewallEntry.isAttached())
		// throw new IllegalStateException();
		//
		// for (FirewallManager firewallManager : server.getRecursiveChildrenOfType(FirewallManager.class)) {
		// firewallManager.doItemOperation(operation, firewallEntry);
		// }

		// IpTablesManager manager = new IpTablesManager();
		IpTablesFirewallManager iptables = new IpTablesFirewallManager();

		IpTablesFirewallState firewallState = iptables.getCurrentFirewallState(target);

		FirewallRecord firewallRule = rule.get();

		if (firewallRule != null) {
			boolean hasRule = firewallState.hasRule(firewallRule);

			if (!OpsContext.isDelete()) {
				if (!hasRule) {
					iptables.configureAddRule(target, firewallRule);
				}
			}

			if (OpsContext.isDelete()) {
				if (hasRule) {
					iptables.configureRemoveRule(target, firewallRule);
				}
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + rule;
	}

	// /**
	// * Allow communication from systems on the same local private network to the specified port on the target machine
	// *
	// * @param opsSystem
	// * @param port
	// * @return
	// */
	// public static List<FirewallEntry> buildInternalNetworkRule(OpsItem item, int localPort, Protocol protocol,
	// String[] ec2Groups) {
	// OpsSystem opsSystem = item.getOpsSystem();
	//
	// if (ServerTypeUtils.isEc2(item)) {
	// return buildBidirectionalLocalPort(opsSystem, localPort, protocol, ec2Groups);
	// } else {
	// List<FirewallEntry> entries = buildBidirectionalLocalPort(opsSystem, localPort, protocol,
	// FirewallNetmask.LocalPrivateNetwork);
	// return entries;
	// }
	// }
	//
	// public static List<FirewallEntry> buildPublicLocalPort(OpsSystem opsSystem, int port, Protocol protocol) {
	// return buildBidirectionalLocalPort(opsSystem, port, protocol, FirewallNetmask.Public);
	// }
	//
	// private static List<FirewallEntry> buildBidirectionalLocalPort(OpsSystem opsSystem, int localPort, Protocol
	// protocol, FirewallNetmask remoteNetmask) {
	// if (localPort == 0)
	// throw new IllegalStateException();
	//
	// List<FirewallEntry> entries = Lists.newArrayList();
	//
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickLocalPort(Direction.In, localPort,
	// protocol, remoteNetmask)));
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickLocalPort(Direction.Out, localPort,
	// protocol, remoteNetmask)));
	//
	// return entries;
	// }
	//
	// private static List<FirewallEntry> buildBidirectionalLocalPort(OpsSystem opsSystem, int localPort, Protocol
	// protocol, String[] ec2Groups) {
	// if (localPort == 0)
	// throw new IllegalStateException();
	//
	// if (ec2Groups.length == 0)
	// throw new IllegalArgumentException("Ec2Groups was empty");
	//
	// List<FirewallEntry> entries = Lists.newArrayList();
	// Set<String> distinctGroups = Sets.newHashSet(ec2Groups);
	//
	// for (String ec2Group : distinctGroups) {
	// if (ec2Group == null)
	// continue;
	//
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickLocalPort(Direction.In, localPort,
	// protocol, FirewallNetmask.buildAwsIdentifierFathomDB(ec2Group))));
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickLocalPort(Direction.Out, localPort,
	// protocol, FirewallNetmask.buildAwsIdentifierFathomDB(ec2Group))));
	// }
	//
	// if (entries.size() == 0)
	// throw new IllegalArgumentException("No rules were produced");
	//
	// return entries;
	// }
	//
	// private static List<FirewallEntry> buildBidirectionalRemotePort(OpsSystem opsSystem, int remotePort, Protocol
	// protocol, FirewallNetmask remoteNetmask) {
	// if (remotePort == 0)
	// throw new IllegalStateException();
	//
	// List<FirewallEntry> entries = Lists.newArrayList();
	//
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickRemotePort(Direction.In, remotePort,
	// protocol, remoteNetmask)));
	// entries.add(FirewallEntry.build(opsSystem, FirewallRecord.buildPassQuickRemotePort(Direction.Out, remotePort,
	// protocol, remoteNetmask)));
	//
	// return entries;
	// }
	//
	// public static List<FirewallEntry> buildAwsAccessLocalPort(OpsSystem opsSystem, String awsIdentifier, int port,
	// Protocol protocol) {
	// return buildBidirectionalLocalPort(opsSystem, port, protocol, FirewallNetmask.buildAwsIdentifier(awsIdentifier));
	// }
	//
	// /**
	// * Allow trafic *to* this machine on the specified port (on the local server)
	// *
	// * @param opsSystem
	// * @param cidr
	// * @param port
	// * @param protocol
	// * @return
	// */
	// public static List<FirewallEntry> buildBidirectionalLocalPort(OpsSystem opsSystem, String cidr, int port,
	// Protocol protocol) {
	// return buildBidirectionalLocalPort(opsSystem, port, protocol, FirewallNetmask.buildCidr(cidr));
	// }
	//
	// /**
	// * Allow traffic *from* this machine to the specified port (on a remote server)
	// *
	// * @param opsSystem
	// * @param remoteCidr
	// * @param port
	// * @param protocol
	// * @return
	// */
	// public static List<FirewallEntry> buildBidirectionalRemotePort(OpsSystem opsSystem, String remoteCidr, int port,
	// Protocol protocol) {
	// return buildBidirectionalRemotePort(opsSystem, port, protocol, FirewallNetmask.buildCidr(remoteCidr));
	// }
	//
	// @SupportsStandardOperations({ StandardOperation.Configure, StandardOperation.Validate,
	// StandardOperation.Initialize })
	// public void doConfigureValidateInitialize(Operation operation) throws Exception {
	// switch (operation.getStandardOperation()) {
	// case Configure:
	// case Initialize:
	// case Validate:
	// FirewallConfiguration.doItemOperation(operation, this);
	// break;
	// default:
	// break;
	// }
	// }
}
