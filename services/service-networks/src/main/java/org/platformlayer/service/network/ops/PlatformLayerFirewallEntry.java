package org.platformlayer.service.network.ops;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.InetAddressChooser;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.FirewallEntry;
import org.platformlayer.ops.firewall.FirewallRecord;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.firewall.FirewallRecord.Transport;
import org.platformlayer.ops.firewall.PortAddressFilter;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tree.LateBound;
import org.platformlayer.ops.tree.OpsTreeBase;

import com.google.common.base.Strings;

public class PlatformLayerFirewallEntry extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PlatformLayerFirewallEntry.class);

	public PlatformLayerKey destItem;
	public PlatformLayerKey sourceItemKey;
	public String sourceCidr;
	public int port;
	public Protocol protocol = Protocol.Tcp;
	public Transport transport = null;

	@Inject
	PlatformLayerHelpers platformLayerHelpers;

	@Inject
	InstanceHelpers instanceHelpers;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		// TODO: Need to register a dependency on destItem?
		MachineResolver dest = MachineResolver.build(destItem);
		addChild(dest);

		List<Transport> transports;

		if (transport == null) {
			String cidr = sourceCidr;
			if (!Strings.isNullOrEmpty(sourceCidr)) {
				IpRange range = IpRange.parse(cidr);
				if (range.isIpv6()) {
					transport = Transport.Ipv6;
				} else {
					transport = Transport.Ipv4;
				}
			}
		}

		if (transport == null) {
			transports = Transport.all();
		} else {
			transports = Collections.singletonList(transport);
		}

		for (final Transport transport : transports) {
			if (!Strings.isNullOrEmpty(sourceCidr)) {
				PortAddressFilter destFilter = PortAddressFilter.withPortRange(port, port);
				PortAddressFilter srcFilter = PortAddressFilter.withCidr(sourceCidr);
				FirewallRecord destRule = FirewallRecord.pass().protocol(protocol).in().dest(destFilter)
						.source(srcFilter);
				destRule.setTransport(transport);

				FirewallEntry entry = FirewallEntry.build(destRule);
				dest.addChild(entry);
			}

			if (sourceItemKey != null) {
				LateBound<FirewallEntry> entry = new LateBound<FirewallEntry>() {
					@Override
					public FirewallEntry get() throws OpsException {
						ItemBase sourceItem = platformLayerHelpers.getItem(sourceItemKey);

						NetworkPoint targetNetworkPoint = NetworkPoint.forTargetInContext();

						boolean required = !OpsContext.isDelete();
						Machine sourceMachine = instanceHelpers.getMachine(sourceItem, required);
						if (sourceMachine == null) {
							// TODO: Store by key? Delete by key?
							log.warn("Source machine not found for firewall rule; assuming already deleted");
							return null;
						}

						PortAddressFilter destFilter = PortAddressFilter.withPortRange(port, port);
						FirewallRecord destRule = FirewallRecord.pass().protocol(protocol).in().dest(destFilter);

						if (transport == Transport.Ipv4) {
							InetAddress address = sourceMachine.getBestAddress(targetNetworkPoint, port,
									InetAddressChooser.preferIpv4());
							if (InetAddressUtils.isIpv6(address)) {
								return null;
							}

							PortAddressFilter srcFilter = PortAddressFilter.withCidr(address.getHostAddress() + "/32");
							destRule = destRule.source(srcFilter).setTransport(transport);
						} else {
							InetAddress address = sourceMachine.getBestAddress(targetNetworkPoint, port,
									InetAddressChooser.preferIpv6());
							if (InetAddressUtils.isIpv4(address)) {
								return null;
							}

							PortAddressFilter srcFilter = PortAddressFilter.withCidr(address.getHostAddress() + "/128");
							destRule = destRule.source(srcFilter).setTransport(transport);
						}

						FirewallEntry entry = FirewallEntry.build(destRule);
						return entry;
					}
				};

				dest.addChild(entry);
			}
		}

		// TODO: Add source rules??
	}
}
