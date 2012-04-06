//package org.platformlayer.service.network.ops;
//
//import javax.inject.Inject;
//
//import org.platformlayer.OpenstackClientException;
//import org.platformlayer.PlatformLayerClient;
//import org.platformlayer.PlatformLayerKey;
//import org.platformlayer.core.model.ItemBase;
//import org.platformlayer.core.model.Tag;
//import org.platformlayer.ops.Machine;
//import org.platformlayer.ops.OpsContext;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.OpsSystem;
//import org.platformlayer.ops.firewall.FirewallEntry;
//import org.platformlayer.ops.firewall.FirewallRecord;
//import org.platformlayer.ops.helpers.InstanceHelpers;
//import org.platformlayer.ops.machines.PlatformLayerHelpers;
//import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
//import org.platformlayer.ops.networks.NetworkPoint;
//import org.platformlayer.service.dns.v1.DnsRecord;
//import org.platformlayer.service.network.model.Endpoint;
//
//import com.google.common.base.Strings;
//
//public class EndpointController extends OpsTreeBase {
//    @Inject
//    PlatformLayerClient platformLayer;
//
//    @Inject
//    InstanceHelpers instanceHelpers;
//
//    @Inject
//    PlatformLayerHelpers platformLayerHelpers;
//
//    @Inject
//    OpsSystem opsSystem;
//
//    @Override
//    protected void addChildren() throws OpsException {
//        Endpoint model = OpsContext.get().getInstance(Endpoint.class);
//
//        PlatformLayerKey itemKey = OpsSystem.toKey(model.item);
//
//        ItemBase dest = platformLayerHelpers.getItem(itemKey);
//
//        MachineResolver resolver = injected(MachineResolver.class);
//        resolver.key = itemKey;
//        addChild(resolver);
//
//        if (model.defaultBlocked) {
//            // Block on machine's firewall
//
//            resolver.addChild(FirewallEntry.build(FirewallRecord.buildBlockPort(model.backendPort)));
//        }
//
//        {
//            CloudPortMapping portMapping = injected(CloudPortMapping.class);
//            resolver.addChild(portMapping);
//        }
//
//        if (!Strings.isNullOrEmpty(model.dnsName)) {
//            // Point dns to machine
//            DnsRecord dnsRecord = new DnsRecord();
//            dnsRecord.setDnsName(model.dnsName);
//
//            Tag parentTag = opsSystem.createParentTag(model);
//            dnsRecord.getTags().add(parentTag);
//
//            // TODO: Use MachineResolver instead??
//            Machine machine = instanceHelpers.getMachine(dest);
//            // OpsTarget target = instanceHelpers.getTarget(dest, machine);
//
//            String address = machine.getAddress(NetworkPoint.forNetwork(model.network), 0);
//            dnsRecord.getAddress().add(address);
//
//            String seq = "0";
//            dnsRecord.id = parentTag.getValue() + "::" + seq;
//
//            try {
//                platformLayer.put(dnsRecord);
//            } catch (OpenstackClientException e) {
//                throw new OpsException("Error registering dns entry", e);
//            }
//        }
//    }
// }
