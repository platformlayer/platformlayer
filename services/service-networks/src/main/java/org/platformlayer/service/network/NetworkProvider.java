package org.platformlayer.service.network;

import java.util.List;
import java.util.Set;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.ops.networks.IpRange;
import org.platformlayer.ops.networks.IpV6Range;
import org.platformlayer.service.network.model.PrivateNetwork;
import org.platformlayer.service.network.model.PrivateNetworkConnection;
import org.platformlayer.service.network.ops.PrivateNetworkHelpers;
import org.platformlayer.xaas.Service;

import com.google.common.collect.Sets;

@Service("network")
public class NetworkProvider extends ServiceProviderBase {

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		if (item instanceof PrivateNetworkConnection) {
			populatePrivateNetworkConnection((PrivateNetworkConnection) item);
		}
		super.beforeCreateItem(item);
	}

	private void populatePrivateNetworkConnection(PrivateNetworkConnection item) throws OpsException {
		if (item.network == null) {
			throw new OpsException("Network is required");
		}

		if (item.machine == null) {
			throw new OpsException("machine is required");
		}

		PrivateNetworkHelpers helper = Injection.getInstance(PrivateNetworkHelpers.class);
		List<PrivateNetworkConnection> connections = helper.getConnections(item.network);

		PrivateNetwork network = helper.getPrivateNetwork(item.network);

		Set<String> tunnelIds = Sets.newHashSet();
		Set<IpV6Range> cidrs = Sets.newHashSet();
		for (PrivateNetworkConnection connection : connections) {
			tunnelIds.add(connection.tunnelId);
			cidrs.add((IpV6Range) IpRange.parse(connection.cidr));
		}

		if (item.tunnelId == null) {
			for (int i = 0;; i++) {
				String tunnelId = String.valueOf(i);
				if (tunnelIds.contains(tunnelId)) {
					continue;
				}
				item.tunnelId = tunnelId;
			}
		} else {
			if (tunnelIds.contains(item.tunnelId)) {
				throw new OpsException("tunnelId already in use");
			}
		}

		if (item.cidr == null) {
			IpV6Range networkRange = (IpV6Range) IpRange.parse(network.cidr);

			for (IpV6Range range : networkRange.listSubnets(16)) {
				boolean overlap = false;

				for (IpV6Range cidr : cidrs) {
					if (range.overlaps(cidr)) {
						overlap = true;
						break;
					}
				}

				if (!overlap) {
					item.cidr = range.toCidr();
					break;
				}
			}

			if (item.cidr == null) {
				throw new OpsException("Unable to assign a subnet (no subnets left?)");
			}
		} else {
			throw new UnsupportedOperationException("Verification of direct-assigned CIDRs not yet implemented");
		}

	}
}
