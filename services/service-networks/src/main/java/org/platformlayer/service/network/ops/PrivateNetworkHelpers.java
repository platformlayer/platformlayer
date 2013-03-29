package org.platformlayer.service.network.ops;

import java.util.List;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.instances.ThrowingProvider;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.network.model.PrivateNetwork;
import org.platformlayer.service.network.model.PrivateNetworkConnection;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class PrivateNetworkHelpers {
	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	InstanceHelpers instanceHelpers;

	public List<PrivateNetworkConnection> getConnections(PlatformLayerKey networkKey) throws OpsException {
		List<PrivateNetworkConnection> connections = Lists.newArrayList();
		for (PrivateNetworkConnection connection : platformLayer.listItems(PrivateNetworkConnection.class)) {
			if (!Objects.equal(connection.network, networkKey)) {
				continue;
			}

			connections.add(connection);
		}
		return connections;
	}

	public ThrowingProvider<String> findTunnelAddress(final PlatformLayerKey machineKey) {
		final NetworkPoint src = NetworkPoint.forPublicInternet();

		return new ThrowingProvider<String>() {

			@Override
			public String build() throws OpsException {
				ItemBase dest = platformLayer.getItem(machineKey);

				Machine machine = instanceHelpers.getMachine(dest, true);

				String address = machine.getNetworkPoint().getBestAddress(src);
				return address;
			}
		};
	}

	public PrivateNetwork getPrivateNetwork(PlatformLayerKey network) throws OpsException {
		return platformLayer.getItem(network, PrivateNetwork.class);
	}

}
