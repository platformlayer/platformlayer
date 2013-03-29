package org.platformlayer.service.network.ops;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.firewall.scripts.IptablesFilterEntry;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.tree.LateBound;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class PlatformLayerFirewallEntry extends OpsTreeBase {
	private static final Logger log = LoggerFactory.getLogger(PlatformLayerFirewallEntry.class);

	public PlatformLayerKey destItem;
	public PlatformLayerKey sourceItemKey;
	public String sourceCidr;
	public int port;
	public Protocol protocol = Protocol.Tcp;
	public Transport transport = null;

	public String uniqueId;

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
				IptablesFilterEntry entry = dest.addChild(IptablesFilterEntry.class);
				entry.port = port;
				entry.sourceCidr = sourceCidr;
				entry.protocol = protocol;
				entry.transport = transport;
				entry.ruleKey = uniqueId;
			} else if (sourceItemKey != null) {
				LateBound<IptablesFilterEntry> entry = new LateBound<IptablesFilterEntry>() {
					@Override
					public IptablesFilterEntry get() throws OpsException {
						ItemBase sourceItem = platformLayerHelpers.getItem(sourceItemKey);

						NetworkPoint targetNetworkPoint = NetworkPoint.forTargetInContext();

						boolean required = !OpsContext.isDelete();
						Machine sourceMachine = instanceHelpers.getMachine(sourceItem, required);
						if (sourceMachine == null) {
							// TODO: Store by key? Delete by key?
							log.warn("Source machine not found for firewall rule; assuming already deleted");
							return null;
						}

						String sourceCidr = null;

						List<InetAddress> addresses = sourceMachine.getNetworkPoint().findAddresses(targetNetworkPoint);
						if (transport == Transport.Ipv4) {
							Iterables.removeIf(addresses, InetAddressUtils.IS_IPV6);

							if (addresses.size() == 1) {
								sourceCidr = addresses.get(0).getHostAddress() + "/32";
							} else {
								if (addresses.isEmpty()) {
									return null;
								}
								throw new IllegalStateException("Not implemented");
							}
						} else {
							Iterables.removeIf(addresses, InetAddressUtils.IS_IPV4);

							if (addresses.size() == 1) {
								sourceCidr = addresses.get(0).getHostAddress() + "/128";
							} else {
								if (addresses.isEmpty()) {
									return null;
								}
								throw new IllegalStateException("Not implemented");
							}
						}

						IptablesFilterEntry entry = injected(IptablesFilterEntry.class);
						entry.port = port;
						entry.sourceCidr = sourceCidr;
						entry.protocol = protocol;
						entry.transport = transport;
						entry.ruleKey = uniqueId;

						return entry;
					}

					@Override
					public String getDescription() throws Exception {
						return "Firewall rules";
					}
				};

				dest.addChild(entry);
			} else {
				// Both empty => wildcard

				IptablesFilterEntry entry = dest.addChild(IptablesFilterEntry.class);
				entry.port = port;
				entry.protocol = protocol;
				entry.transport = transport;
				entry.ruleKey = uniqueId;
			}
		}

		// TODO: Add source rules??
	}
}
