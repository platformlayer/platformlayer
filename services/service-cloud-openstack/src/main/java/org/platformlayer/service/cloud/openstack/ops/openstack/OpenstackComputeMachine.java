package org.platformlayer.service.cloud.openstack.ops.openstack;

import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.client.InstanceState;
import org.openstack.model.compute.Addresses.Network.Ip;
import org.openstack.model.compute.Server;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.MachineBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class OpenstackComputeMachine extends MachineBase {
	static final Logger log = Logger.getLogger(OpenstackComputeMachine.class);

	final OpenstackCloudContext cloudContext;
	final OpenstackCloud cloud;
	// final String openstackServerId;
	// final String ipAddress;
	private final Server server;

	public OpenstackComputeMachine(OpenstackCloudContext cloudContext, OpenstackCloud cloud, Server server) {
		this.cloudContext = cloudContext;
		this.cloud = cloud;
		this.server = server;
	}

	@Override
	public void terminate() throws OpsException {
		cloudContext.terminateInstance(this);
	}

	public String getOpenstackServerId() {
		return server.getId();
	}

	// @Override
	// public String getServerId() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public PlatformLayerKey getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String findAddress(NetworkPoint src, int destinationPort) {
		// TODO: Check private networks

		OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();

		// We assume that private networks can still reach the public internet, so these work for everyone
		List<Ip> publicIps = helpers.findPublicIps(cloud, server);
		for (Ip ip : publicIps) {
			if (Objects.equal("6", ip.getVersion())) {
				continue;
			}
			String addr = ip.getAddr();
			if (!Strings.isNullOrEmpty(addr)) {
				return addr;
			}
		}

		// {
		// String accessIPv4 = server.getAccessIpV4();
		// if (!Strings.isNullOrEmpty(accessIPv4))
		// return accessIPv4;
		// }

		// Addresses addresses = server.getAddresses();
		// if (addresses != null) {
		// for (Network network : addresses.getNetworks()) {
		// String networkId = network.getId();
		// // TODO: Check private network
		// for (Ip ip : network.getIps()) {
		// String ipType = ip.getVersion();
		// // ipType is "4" or "6".
		// // TODO: Check
		// String addr = ip.getAddr();
		// if (!Strings.isNullOrEmpty(addr))
		// return addr;
		// }
		// }
		// }

		// String privateNetworkId = src.getPrivateNetworkId();
		// if (Objects.equal(privateNetworkId, NetworkPoint.PRIVATE_NETWORK_ID)) {
		// Tags tags = machine.getTags();
		// for (String address : tags.find(Tag.NETWORK_ADDRESS)) {
		// return address;
		// }
		// }

		return null;
	}

	public List<Tag> buildAddressTags() {
		List<Tag> tags = Lists.newArrayList();

		OpenstackCloudHelpers helpers = new OpenstackCloudHelpers();

		List<Ip> publicIps = helpers.findPublicIps(cloud, server);
		for (Ip ip : publicIps) {
			String addr = ip.getAddr();
			tags.add(new Tag(Tag.NETWORK_ADDRESS, addr));
		}

		return tags;
	}

	public OpenstackCloud getCloud() {
		return cloud;
	}

	public Server getServer() {
		return server;
	}

	@Override
	public boolean isTerminated() {
		InstanceState instanceState = InstanceState.get(server);
		log.debug("isTerminated? State=" + instanceState);
		if (instanceState.isTerminated()) {
			return true;
		}

		if (instanceState.isTerminating()) {
			// TODO: Not sure if this is right
			log.warn("isTerminated mapping isTerminating => isTerminated=true");
			return true;
		}

		return false;
	}

	// @Override
	// public String getServerId() {
	// return "openstack:" + getOpenstackServerId();
	// }

	// public String getState() throws OpsException {
	// return cloudContext.getState(openstackServerId);
	// }
}
