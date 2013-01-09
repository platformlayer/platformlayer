package org.platformlayer.ops.machines;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.*;
import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.MachineBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;

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

	// public String getPublicAddress(ItemBase item) throws OpsException {
	// String address = findPublicAddress(item);
	// if (address == null) {
	// throw new OpsException("Could not determine public address for: " + item);
	// }
	// return address;
	// }

	@Override
	public List<InetAddress> findAddresses(NetworkPoint src, int destinationPort) {
		List<InetAddress> matching = Lists.newArrayList();

		// String privateNetworkId = src.getPrivateNetworkId();
		{
			Tags tags = machine.getTags();
			List<InetAddress> addresses = Tag.NETWORK_ADDRESS.find(tags);

			for (InetAddress address : addresses) {
				if (InetAddressUtils.isPublic(address)) {
					matching.add(address);
				} else {
					if (!src.isPublicAddress()) {
						// They could both be on the same public network
						throw new IllegalStateException("Not implemented");
					}
				}
			}
		}

		// if (src.isPublicInternet())
		// We assume that private networks can still reach the public internet, so these work for everyone
		{
			List<EndpointInfo> endpoints = EndpointInfo.findEndpoints(machine.getTags(), destinationPort);
			if (!endpoints.isEmpty()) {
				for (EndpointInfo endpoint : endpoints) {
					matching.add(endpoint.getAddress());
				}
			}
		}

		return matching;

		// OpsContext ops = OpsContext.get();
		// ModelKey modelKey = ops.buildModelKey(item);
		//
		//
		//
		// // {
		// // String instanceKey = tags.findUnique(Tag.INSTANCE_KEY);
		// //
		// // if (instanceKey != null) {
		// // Machine machine = cloud.findMachineByInstanceKey(instanceKey);
		// // return machine;
		// // }
		// // }
		//
		// {
		// // TODO: Do we have to skip this if we've been passed a PersistentInstances?
		//
		// // String conductorId = ops.buildUrl(modelKey);
		//
		// Tag parentTag = ops.createParentTag(modelKey);
		//
		// // // TODO: Fix this so that we don't get everything...
		// // for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class)) {
		// // String systemId = persistentInstance.getTags().findUnique(Tag.PARENT_ID);
		// // if (Objects.equal(conductorId, systemId)) {
		// // String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
		// // if (instanceKey != null) {
		// // return cloud.findMachineByInstanceKey(instanceKey);
		// // }
		// // }
		// // }
		//
		// // for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class, parentTag))
		// {
		// // String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
		// // if (instanceKey != null) {
		// // return cloud.findMachineByInstanceKey(instanceKey);
		// // }
		// // }
		//
		// }
		// }

		// Tags tags = machine.getTags();
		// for (Tag tag : tags) {
		// if (tag.getKey().equals(Tag.NETWORK_ADDRESS)) {
		// return tag.getValue();
		// }
		// }
		// return null;
		// }
		//
		// return null;
		// }

		// if ()
		// String privateNetworkId = src.getPrivateNetworkId();
		// if (privateNetworkId)
		// Tags tags = machine.getTags();
		// for (Tag tag : tags) {
		// if (tag.getKey().equals(Tag.NETWORK_ADDRESS)) {
		// return tag.getValue();
		// }
		// }
		// return null;
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
}
