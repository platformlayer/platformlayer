//package org.platformlayer.ops.firewall;
//
//import java.rmi.server.Operation;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//import org.platformlayer.ops.OpsException;
//
//import com.google.common.collect.Lists;
//
///**
// * Has children that are FirewallEntry objects. Together, they define the ports that should be open. Unusually for the ops system, it will also remove entries that are not listed.
// * 
// * @author justinsb
// * 
// */
//// @Hook(hookClasses = { OpsServer.class })
//// @Icon("firewall")
//public class FirewallConfiguration {
//    @Override
//    protected void attached() throws OpsException {
//        super.attached();
//
//        DefaultFirewallConfiguration.addDefaultConfiguration(this);
//
//        OpsServer server = smartGetServer(true);
//
//        switch (getOperatingSystem()) {
//        case Solaris:
//            addChild(new IpfFirewallManager(getOpsSystem(), "ipf-manager", null));
//            break;
//        case Linux:
//            addChild(new IpTablesFirewallManager(getOpsSystem(), "iptables-manager", null));
//            break;
//        default:
//            throw new IllegalArgumentException("Unhandled OS: " + getOperatingSystem());
//        }
//
//    }
//
//    static final Logger log = Logger.getLogger(FirewallConfiguration.class);
//
//    public void addRule(String rule) throws OpsException {
//        FirewallRecord record = FirewallRecordParser.parseRule(rule);
//        addRule(record);
//    }
//
//    public void addRule(FirewallRecord record) throws OpsException {
//        this.addChild(FirewallEntry.build(getOpsSystem(), record));
//    }
//
//    public OpsItem mapToChild(FirewallRecord record) {
//        for (FirewallEntry entry : this.getChildrenOfType(FirewallEntry.class)) {
//            if (record.equals(entry.getRule()))
//                return entry;
//        }
//        throw new IllegalStateException("Cannot find corresponding firewall entry: " + record);
//    }
//
//    public List<FirewallRecord> getDesiredConfiguration() {
//        OpsServer server = smartGetServer(true);
//
//        List<FirewallRecord> desired = Lists.newArrayList();
//        for (FirewallEntry entry : getOpsSystem().getAllForServer(server, FirewallEntry.class)) {
//            desired.add(entry.getRule());
//        }
//
//        return desired;
//    }
//
//    // public static void notifyChange(FirewallEntry firewallEntry) {
//    // OpsServer server = firewallEntry.smartGetServer();
//    // if (server == null)
//    // return;
//    //
//    // for (FirewallConfiguration configuration : server.getRecursiveChildrenOfType(FirewallConfiguration.class)) {
//    // configuration.notifyPolicyChange(firewallEntry);
//    // }
//    // }
//
//    // public void notifyPolicyChange(Operation operation, FirewallEntry firewallEntry) {
//    // OpsServer server = smartGetServer();
//    // if (server == null)
//    // return;
//    //
//    // if (!firewallEntry.isAttached())
//    // return;
//    //
//    // for (FirewallManager ipfManager : getRecursiveChildrenOfType(FirewallManager.class)) {
//    // try {
//    // ipfManager.doItemConfigureValidate(operation, firewallEntry);
//    // } catch (Exception e) {
//    // log.info("Unexpected error adding ipf rule", e);
//    // }
//    // }
//    // }
//
//    public static void doItemOperation(Operation operation, FirewallEntry firewallEntry) throws Exception {
//        OpsServer server = firewallEntry.smartGetServer();
//        if (server == null)
//            return;
//
//        if (!firewallEntry.isAttached())
//            throw new IllegalStateException();
//
//        for (FirewallManager firewallManager : server.getRecursiveChildrenOfType(FirewallManager.class)) {
//            firewallManager.doItemOperation(operation, firewallEntry);
//        }
//    }
//
//    public void addPublicServiceTcpPort(int port) throws OpsException {
//        addRule("pass in quick proto tcp from any to any port = " + port);
//        addRule("pass out quick proto tcp from any port = " + port + " to any");
//    }
//
//    public void addPublicServiceUdpPort(int port) throws OpsException {
//        addRule("pass in quick proto udp from any to any port = " + port);
//        addRule("pass out quick proto udp from any port = " + port + " to any");
//    }
//
//    public void addClientUdpPort(int port) throws OpsException {
//        addRule("pass in quick proto udp from any port = " + port + " to any");
//        addRule("pass out quick proto udp from any to any port = " + port);
//    }
//
//    public void addClientTcpPort(int port) throws OpsException {
//        addRule("pass in quick proto tcp from any port = " + port + " to any");
//        addRule("pass out quick proto tcp from any to any port = " + port);
//    }
//
// }
