package org.platformlayer.service.network.ops;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.instances.ThrowingProvider;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreTunnel extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(GreTunnel.class);

	public String tunnelName;
	public ThrowingProvider<String> remoteTunnelAddress;
	public String remotePrivateNetwork;
	public ThrowingProvider<String> localTunnelAddress;
	public String localPrivateAddress;

	@Handler
	public void handler(OpsTarget target) throws OpsException {
		// ip tunnel add netb mode gre remote 184.173.172.26 local 209.105.243.38 ttl 255
		Command addTunnel = Command.build("ip tunnel add {0} mode gre remote {1} local {2} ttl 255", tunnelName,
				remoteTunnelAddress, localTunnelAddress);
		target.executeCommand(addTunnel);

		// ip addr add fdfd:fdfd:fdfd:1::/64 dev netb
		Command tunnelAddAddress = Command.build("ip addr add {0} dev {1}", localPrivateAddress, tunnelName);
		target.executeCommand(tunnelAddAddress);

		// ip route add fdfd:fdfd:fdfd:1::/64 dev netb
		Command tunnelAddRoute = Command.build("ip route add {0} dev {1}", remotePrivateNetwork, tunnelName);
		target.executeCommand(tunnelAddRoute);

		Command tunnelUp = Command.build("ip link set {0} up", tunnelName);
		target.executeCommand(tunnelUp);
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
