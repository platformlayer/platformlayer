//package org.platformlayer.service.mysql.ops;
//
//import java.util.List;
//
//import org.platformlayer.core.model.AccessPolicy;
//import org.platformlayer.core.model.AccessPolicySet;
//import org.platformlayer.core.model.ItemBase;
//import org.platformlayer.ops.Handler;
//import org.platformlayer.ops.OperationType;
//import org.platformlayer.ops.OpsContext;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsTarget;
//import org.platformlayer.ops.firewall.FirewallNetmask;
//import org.platformlayer.ops.firewall.FirewallRecord;
//import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
//import org.platformlayer.ops.firewall.IpTablesFirewallManager;
//import org.platformlayer.ops.firewall.PortAddressFilter;
//import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
//
//import com.google.common.collect.Lists;
//
//public class NetworkAccessPolicy extends OpsTreeBase {
//    public AccessPolicySet accessPolicySet;
//    public int[] ports;
//
//    public static NetworkAccessPolicy build(ItemBase item, int ports) throws OpsException {
//        return build(item, new int[] { ports });
//    }
//
//    public static NetworkAccessPolicy build(ItemBase item, int[] ports) throws OpsException {
//        NetworkAccessPolicy policy = injected(NetworkAccessPolicy.class);
//        policy.accessPolicySet = item.accessPolicySet;
//        policy.ports = ports;
//        return policy;
//    }
//
//    private static FirewallRecord buildAllow(AccessPolicy accessPolicy, int port) {
//        PortAddressFilter srcFilter = new PortAddressFilter();
//        srcFilter.setNetmask(FirewallNetmask.buildCidr(accessPolicy.netmask));
//        return FirewallRecord.pass().dest(PortAddressFilter.withPortRange(port, port)).source(srcFilter).in().protocol(Protocol.Tcp);
//    }
//
//    private static FirewallRecord buildDenyPort(int port) {
//        return FirewallRecord.block().dest(PortAddressFilter.withPortRange(port, port)).in().protocol(Protocol.Tcp);
//    }
//
//    @Override
//    protected void addChildren() throws OpsException {
//
//    }
//
//    @Handler
//    public void handler(OpsTarget target) throws OpsException {
//        IpTablesFirewallManager iptables = new IpTablesFirewallManager();
//
//        for (int port : ports) {
//            List<FirewallRecord> records = Lists.newArrayList();
//
//            for (AccessPolicy accessPolicy : accessPolicySet.policies) {
//                records.add(buildAllow(accessPolicy, port));
//            }
//
//            records.add(buildDenyPort(port));
//
//            OperationType operationType = OpsContext.get().getOperationType();
//            iptables.configureRules(target, operationType, port, records);
//        }
//
//    }
// }
