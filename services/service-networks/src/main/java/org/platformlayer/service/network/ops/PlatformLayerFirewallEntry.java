package org.platformlayer.service.network.ops;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.FirewallEntry;
import org.platformlayer.ops.firewall.FirewallRecord;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.firewall.PortAddressFilter;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.networks.NetworkPoint;

import com.google.common.base.Strings;

public class PlatformLayerFirewallEntry extends OpsTreeBase {
    public PlatformLayerKey destItem;
    public PlatformLayerKey sourceItemKey;
    public String sourceCidr;
    public int port;
    public Protocol protocol = Protocol.Tcp;
    
    @Inject
    PlatformLayerHelpers platformLayerHelpers;

    @Inject
    InstanceHelpers instanceHelpers;

    @Handler
    public void handler() {
    }

    @Override
    protected void addChildren() throws OpsException {
        MachineResolver dest = MachineResolver.build(destItem);
        addChild(dest);

        if (!Strings.isNullOrEmpty(sourceCidr)) {
            PortAddressFilter destFilter = PortAddressFilter.withPortRange(port, port);
            PortAddressFilter srcFilter = PortAddressFilter.withCidr(sourceCidr);
            FirewallRecord destRule = FirewallRecord.pass().protocol(protocol).in().dest(destFilter).source(srcFilter);
            FirewallEntry entry = FirewallEntry.build(destRule);
            dest.addChild(entry);
        }

        if (sourceItemKey != null) {
            Provider<FirewallRecord> ruleProvider = new Provider<FirewallRecord>() {
                @Override
                public FirewallRecord get() {
                    try {
                        ItemBase sourceItem = platformLayerHelpers.getItem(sourceItemKey);

                        NetworkPoint targetNetworkPoint = NetworkPoint.forTargetInContext();
                        Machine sourceMachine = instanceHelpers.getMachine(sourceItem);
                        String address = sourceMachine.getAddress(targetNetworkPoint, port);
                        PortAddressFilter destFilter = PortAddressFilter.withPortRange(port, port);
                        PortAddressFilter srcFilter = PortAddressFilter.withCidr(address + "/32");
                        FirewallRecord destRule = FirewallRecord.pass().protocol(protocol).in().dest(destFilter).source(srcFilter);
                        return destRule;
                    } catch (OpsException e) {
                        throw new IllegalArgumentException("Error building network rule", e);
                    }
                }
            };

            FirewallEntry entry = FirewallEntry.build(ruleProvider);
            dest.addChild(entry);
        }

        // TODO: Add source rules??
    }
}
