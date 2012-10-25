package org.platformlayer.ops.firewall;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.SetUtils;
import org.platformlayer.SetUtils.SetCompareResults;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.collect.Lists;

/**
 * Defines the application of the firewall configuration to a real firewall (IPF, EC2, maybe hardware firewalls in
 * future)
 * 
 * @author justinsb
 * 
 */
public abstract class FirewallManager {
	static final Logger log = Logger.getLogger(FirewallManager.class);

	private static final boolean DUMP_CONFIG = false;

	public void configureRules(OpsTarget target, int port, List<FirewallRecord> desired) throws OpsException {
		List<FirewallRecord> actual = getConfiguredRules(target, port);

		if (DUMP_CONFIG || true) {
			log.debug("Actual configuration:");
			for (FirewallRecord rule : actual) {
				log.debug("\t" + rule);
			}
		}

		SetCompareResults<FirewallRecord> setCompareResults = SetUtils.setCompare(desired, actual);
		// LEFT= desired
		// RIGHT= actual

		if (!setCompareResults.leftNotRight.isEmpty() || !setCompareResults.rightNotLeft.isEmpty()) {

			List<FirewallRecord> deferredAdd = Lists.newArrayList();

			for (FirewallRecord add : setCompareResults.leftNotRight) {
				if (OpsContext.isConfigure()) {
					if (!add.isQuick()) {
						// We add these default rules last, so that we can have all our non-default rules in place
						// This is particularly important for block, with IpTables
						log.info("Deferring add of firewall entry: " + add);
						deferredAdd.add(add);
					} else {
						log.info("Adding firewall entry: " + add);
						configureAddRule(target, add);
					}
				} else if (OpsContext.isValidate()) {
					OpsContext.get().addWarning(this, "Firewall rule not found: {1}", add);
				}
			}

			for (FirewallRecord remove : setCompareResults.rightNotLeft) {
				if (OpsContext.isConfigure()) {
					log.info("Removing firewall entry: " + remove);
					configureRemoveRule(target, remove);
				} else if (OpsContext.isValidate()) {
					OpsContext.get().addWarning(this, "Extra firewall rule found: {1}", remove);
				}
			}

			for (FirewallRecord add : deferredAdd) {
				if (OpsContext.isConfigure()) {
					log.info("Adding firewall entry: " + add);
					configureAddRule(target, add);
				}
			}
		}

		// if (isConfigure) {
		// afterChangeConfiguration(desired);
		// }

		List<FirewallRecord> duplicates = findDuplicates(target);

		if (OpsContext.isValidate()) {
			for (FirewallRecord duplicate : duplicates) {
				log.warn("Duplicate rule found: " + duplicate);
			}
		}

		if (OpsContext.isForce()) {
			for (FirewallRecord duplicate : duplicates) {
				configureRemoveRule(target, duplicate);
			}
		}
	}

	// public TimeSpan minimumUptime = FathomConfig.getTimeSpan("FirewallManager.minimumUptime", "5m");
	//
	//
	// private static final boolean DUMP_CONFIG = FathomConfig.getBoolean("FirewallManager.dumpConfig", false);
	//
	// @SupportsStandardOperations({ StandardOperation.Configure, StandardOperation.Validate,
	// StandardOperation.ForceConfigure })
	// public void doConfigureValidate() throws Exception {
	// if (!getOpsSystem().isSystemReadyAndHealthy(minimumUptime) && !operation.isForce()) {
	// return;
	// }
	//
	// List<FirewallRecord> desired = getDesiredConfiguration();
	// List<FirewallRecord> actual = getConfiguredRules();
	//
	// if (DUMP_CONFIG || true) {
	// log.debug("Actual configuration:");
	// for (FirewallRecord rule : actual) {
	// log.debug("\t" + rule);
	// }
	// }
	//
	// SetCompareResults<FirewallRecord> setCompareResults = SetUtils.setCompare(desired, actual);
	// // LEFT= desired
	// // RIGHT= actual
	//
	// if (!setCompareResults.leftNotRight.isEmpty() || !setCompareResults.rightNotLeft.isEmpty()) {
	//
	// List<FirewallRecord> deferredAdd = Lists.newArrayList();
	//
	// for (FirewallRecord add : setCompareResults.leftNotRight) {
	// if (operation.isConfigure()) {
	// if (!add.isQuick()) {
	// // We add these default rules last, so that we can have all our non-default rules in place
	// // This is particularly important for block, with IpTables
	// log.info("Deferring add of firewall entry: " + add);
	// deferredAdd.add(add);
	// } else {
	// log.info("Adding firewall entry: " + add);
	// configureAddRule(operation, add);
	// }
	// } else if (operation.isValidate()) {
	// operation.addWarning(this, "FirewallRuleNotFound-" + NodeUtils.sanitizeKeyName(add.buildKey()),
	// "Firewall rule not found: " + add);
	// }
	// }
	//
	// for (FirewallRecord remove : setCompareResults.rightNotLeft) {
	// if (operation.isConfigure()) {
	// log.info("Removing firewall entry: " + remove);
	// configureRemoveRule(operation, remove);
	// } else if (operation.isValidate()) {
	// operation.addWarning(this, "ExtraFirewallRuleFound-" + NodeUtils.sanitizeKeyName(remove.buildKey()),
	// "Extra firewall rule found, should be removed: " + remove);
	// }
	// }
	//
	// for (FirewallRecord add : deferredAdd) {
	// if (operation.isConfigure()) {
	// log.info("Adding firewall entry: " + add);
	// configureAddRule(operation, add);
	// }
	// }
	// }
	//
	// // if (isConfigure) {
	// // afterChangeConfiguration(desired);
	// // }
	//
	// List<FirewallRecord> duplicates = findDuplicates(operation);
	//
	// if (operation.isValidate()) {
	// for (FirewallRecord duplicate : duplicates) {
	// log.warn("Duplicate rule found: " + duplicate);
	// }
	// }
	//
	// if (operation.isForce()) {
	// for (FirewallRecord duplicate : duplicates) {
	// configureRemoveRule(operation, duplicate);
	// }
	// }
	//
	// }

	private List<FirewallRecord> findDuplicates(OpsTarget target) throws OpsException {
		List<FirewallRecord> actual;
		// Remove duplicate rules
		actual = getConfiguredRules(target);
		List<FirewallRecord> once = Lists.newArrayList();
		List<FirewallRecord> duplicates = Lists.newArrayList();
		for (FirewallRecord configured : actual) {
			if (once.contains(configured)) {
				duplicates.add(configured);
			} else {
				once.add(configured);
			}
		}
		return duplicates;
	}

	// public void doItemOperation(OpsTarget target, OperationType operationType, IptablesFirewallEntry firewallEntry)
	// throws Exception {
	// boolean isValidate = operationType.isValidate();
	//
	// FirewallRecord firewallRule = firewallEntry.rule;
	//
	// List<FirewallRecord> desired = rewriteRule(target, firewallRule);
	// List<FirewallRecord> actual = getConfiguredRules(target);
	//
	// if (DUMP_CONFIG) {
	// log.debug("Actual configuration:");
	// for (FirewallRecord rule : actual) {
	// log.debug("\t" + rule);
	// }
	// }
	//
	// SetCompareResults<FirewallRecord> setCompareResults = SetUtils.setCompare(desired, actual);
	// // LEFT= desired
	// // RIGHT= actual
	//
	// for (FirewallRecord add : setCompareResults.leftNotRight) {
	// if (!isValidate) {
	// log.info("Adding firewall entry: " + add);
	// configureAddRule(target, add);
	// } else {
	// OpsContext.get().addWarning(this, "Firewall rule not found {0}", add);
	// }
	// }
	//
	// // afterChangeConfiguration(null);
	// }

	// protected List<FirewallRecord> getDesiredConfiguration() throws OpsException, InterruptedException {
	// FirewallConfiguration firewallConfiguration = getAncestor(FirewallConfiguration.class);
	// List<FirewallRecord> desiredConfiguration = firewallConfiguration.getDesiredConfiguration();
	// List<FirewallRecord> rewritten = rewriteRules(smartGetServer(true), desiredConfiguration);
	//
	// if (DUMP_CONFIG) {
	// log.debug("Desired configuration:");
	// for (FirewallRecord rule : rewritten) {
	// log.debug("\t" + rule);
	// }
	// }
	//
	// return rewritten;
	// }
	//
	// // protected abstract void afterChangeConfiguration(List<FirewallRecord> newConfiguration);

	private List<FirewallRecord> getConfiguredRules(OpsTarget target, int port) throws OpsException {
		List<FirewallRecord> matches = Lists.newArrayList();
		for (FirewallRecord rule : getConfiguredRules(target)) {
			PortAddressFilter destFilter = rule.getDestFilter();
			if (destFilter.getPortHigh() == destFilter.getPortLow()) {
				if (destFilter.getPortHigh() == port) {
					matches.add(rule);
				}
			}
		}
		return matches;
	}

	protected abstract void configureRemoveRule(OpsTarget target, FirewallRecord remove) throws OpsException;

	protected abstract void configureAddRule(OpsTarget target, FirewallRecord add) throws OpsException;

	protected abstract List<FirewallRecord> getConfiguredRules(OpsTarget target) throws OpsException;

	// protected List<FirewallRecord> rewriteRules(OpsServer server, List<FirewallRecord> input) throws OpsException,
	// InterruptedException {
	// List<FirewallRecord> rewroteList = Lists.newArrayList();
	// for (FirewallRecord rule : input) {
	// for (FirewallRecord addRule : rewriteRule(server, rule)) {
	// if (!rewroteList.contains(addRule))
	// rewroteList.add(addRule);
	// }
	// }
	// return rewroteList;
	// }

	protected List<FirewallRecord> rewriteRule(OpsTarget target, FirewallRecord rule) throws OpsException,
			InterruptedException {
		// Default rewrite does nothing
		List<FirewallRecord> rules = Lists.newArrayList();
		rules.add(rule);
		return rules;
	}

	// // public void quickAdd(FirewallEntry firewallEntry) throws Exception {
	// // List<FirewallRecord> rules = rewriteRule(smartGetServer(true), firewallEntry.getRule());
	// //
	// // for (FirewallRecord add : rules) {
	// // log.info("Adding firewall entry: " + add);
	// // configureAddRule(operation, add);
	// // }
	// //
	// // // afterChangeConfiguration(null);
	// // }
	//
	// protected void rewriteVirtualAddresses(OpsServer server, FirewallRecord rule, List<FirewallRecord> dest, boolean
	// processSourceFilter) throws OpsException, InterruptedException {
	// PortAddressFilter filter = (processSourceFilter ? rule.getSrcFilter() : rule.getDestFilter());
	//
	// switch (filter.getNetmask().getNetmaskType()) {
	// case AwsIdentifier:
	// if (ServerTypeUtils.isEc2(server)) {
	// // We rely on the EC2 firewall here...
	// FirewallRecord copy = rule.deepCopy();
	//
	// PortAddressFilter copyTargetFilter = (processSourceFilter ? copy.getSrcFilter() : copy.getDestFilter());
	// copyTargetFilter.setNetmask(FirewallNetmask.buildCidr(ServerTypeUtils.EC2_INTERNAL_CIDR));
	//
	// dest.add(copy);
	// } else {
	// throw new IllegalArgumentException("Cannot filter on AWS id outside EC2: " + rule);
	// }
	// break;
	//
	// case LocalPrivateNetwork:
	// String myIpAddress = server.getMyIpAddress();
	//
	// if (ServerTypeUtils.isEc2(server)) {
	// // We rely on the EC2 firewall here...
	// FirewallRecord copy = rule.deepCopy();
	//
	// PortAddressFilter copyTargetFilter = (processSourceFilter ? copy.getSrcFilter() : copy.getDestFilter());
	// copyTargetFilter.setNetmask(FirewallNetmask.buildCidr(ServerTypeUtils.EC2_INTERNAL_CIDR));
	//
	// dest.add(copy);
	// } else {
	// // TODO: Reintroduce IPSEC reliance??
	// FirewallRecord copy = rule.deepCopy();
	//
	// PortAddressFilter copyTargetFilter = (processSourceFilter ? copy.getSrcFilter() : copy.getDestFilter());
	// copyTargetFilter.setNetmask(FirewallNetmask.buildCidr("10.0.0.0/8"));
	//
	// dest.add(copy);
	// }
	//
	// if (!myIpAddress.startsWith("10.")) {
	// FirewallRecord copy = rule.deepCopy();
	//
	// PortAddressFilter copyTargetFilter = (processSourceFilter ? copy.getSrcFilter() : copy.getDestFilter());
	// copyTargetFilter.setNetmask(FirewallNetmask.buildCidr(myIpAddress));
	//
	// dest.add(copy);
	// }
	// break;
	//
	// default:
	// dest.add(rule);
	// break;
	// }
	// }
	//
	// protected String getLoopbackDevice() throws OpsException {
	// switch (getOperatingSystem()) {
	// case Solaris:
	// return "lo0";
	// case Linux:
	// return "lo";
	// default:
	// throw new IllegalStateException();
	// }
	// }
}
