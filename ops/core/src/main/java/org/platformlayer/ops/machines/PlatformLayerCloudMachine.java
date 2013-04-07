package org.platformlayer.ops.machines;

import java.net.InetAddress;
import java.util.List;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.MachineBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class PlatformLayerCloudMachine extends MachineBase {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerCloudMachine.class);

	final PlatformLayerHelpers platformLayerClient;
	final InstanceBase machine;

	public PlatformLayerCloudMachine(PlatformLayerHelpers platformLayerClient, InstanceBase machine) {
		this.platformLayerClient = platformLayerClient;
		this.machine = machine;
	}

	@Override
	public void terminate() throws OpsException {
		platformLayerClient.deleteItem(machine.getKey());

		// context.terminate(machine);
	}

	@Override
	public PlatformLayerKey getKey() {
		return machine.getKey();
	}

	public InstanceBase getInstance() {
		return this.machine;
	}

	@Override
	public boolean isTerminated() {
		ManagedItemState state = machine.getState();
		log.debug("isTerminated? State=" + state);
		switch (state) {
		case DELETE_REQUESTED:
			// TODO: Not sure if this is right
			log.warn("isTerminated mapping DELETE_REQUESTED => isTerminated=true");
			return true;

		case DELETED:
			return true;

		default:
			return false;
		}
	}

	NetworkPoint networkPoint;

	@Override
	public NetworkPoint getNetworkPoint() throws OpsException {
		if (networkPoint == null) {
			List<InetAddress> matching = Lists.newArrayList();

			// String privateNetworkId = src.getPrivateNetworkId();
			{
				Tags tags = machine.getTags();
				List<InetAddress> addresses = Tag.NETWORK_ADDRESS.find(tags);

				for (InetAddress address : addresses) {
					if (InetAddressUtils.isPublic(address)) {
						matching.add(address);
					} else {
						// if (!src.isPublicAddress()) {
						// // They could both be on the same public network
						// throw new IllegalStateException("Not implemented");
						// }
					}
				}
			}

			if (matching.size() != 1) {
				throw new OpsException("Found multiple addresses for: " + getKey() + ": "
						+ Joiner.on(",").join(matching));
			}
			networkPoint = NetworkPoint.forAddress(matching.get(0));

			// // if (src.isPublicInternet())
			// // We assume that private networks can still reach the public internet, so these work for everyone
			// {
			// List<EndpointInfo> endpoints = EndpointInfo.findEndpoints(machine.getTags(), destinationPort);
			// if (!endpoints.isEmpty()) {
			// for (EndpointInfo endpoint : endpoints) {
			// matching.add(endpoint.getAddress());
			// }
			// }
			// }
		}
		return networkPoint;
	}
}
