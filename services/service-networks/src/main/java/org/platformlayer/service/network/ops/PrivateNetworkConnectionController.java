package org.platformlayer.service.network.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.instances.ThrowingProvider;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.network.model.PrivateNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateNetworkConnectionController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PrivateNetworkConnectionController.class);

	@Inject
	OpsContext ops;

	@Inject
	PrivateNetworkHelpers helpers;

	@Inject
	InstanceHelpers instanceHelpers;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		PrivateNetworkConnection me = ops.getInstance(PrivateNetworkConnection.class);

		MachineResolver machine = MachineResolver.build(me.machine);

		// PrivateNetwork privateNetwork = helpers.getPrivateNetwork(me.network);

		ThrowingProvider<String> localTunnelAddress = helpers.findTunnelAddress(me.machine);

		for (PrivateNetworkConnection remote : helpers.getConnections(me.network)) {
			if (remote.getKey().equals(me.getKey())) {
				// Ignore ourselves
				continue;
			}

			GreTunnel tunnel = machine.addChild(GreTunnel.class);
			tunnel.remotePrivateNetwork = remote.cidr;
			tunnel.remoteTunnelAddress = helpers.findTunnelAddress(remote.machine);
			tunnel.localTunnelAddress = localTunnelAddress;
			tunnel.localPrivateAddress = me.cidr;
			tunnel.tunnelName = "gre_" + remote.tunnelId;
		}
	}

}
