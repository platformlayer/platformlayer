package org.platformlayer.service.cloud.google.ops.compute;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.MachineBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.service.cloud.google.model.GoogleCloud;

import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Operation;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class GoogleComputeMachine extends MachineBase {
	static final Logger log = Logger.getLogger(GoogleComputeMachine.class);

	final GoogleComputeClient computeClient;
	final GoogleCloud cloud;

	private Instance instance;

	private boolean terminated;

	public GoogleComputeMachine(GoogleComputeClient computeClient, GoogleCloud cloud, Instance instance) {
		this.computeClient = computeClient;
		this.cloud = cloud;
		this.instance = instance;
		Preconditions.checkNotNull(instance);
	}

	@Override
	public void terminate() throws OpsException {
		try {
			Operation operation = computeClient.terminateInstance(instance.getName());
			computeClient.waitComplete(operation, 5, TimeUnit.MINUTES);
		} catch (TimeoutException e) {
			throw new OpsException("Timeout waiting for instance termination", e);
		}

		refreshState();
	}

	public void refreshState() throws OpsException {
		Instance newState = computeClient.findInstanceByName(instance.getName());

		if (newState == null) {
			// Not found => deleted
			terminated = true;
		} else {
			instance = newState;
		}
	}

	@Override
	public PlatformLayerKey getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<InetAddress> findAddresses(NetworkPoint src, int destinationPort) {
		// TODO: Check private networks

		List<InetAddress> addresses = Lists.newArrayList();

		// We assume that private networks can still reach the public internet, so these work for everyone
		List<String> publicIps = GoogleComputeClient.findPublicIps(instance);
		for (String ip : publicIps) {
			// if (Objects.equal("6", ip.getVersion())) {
			// continue;
			// }

			if (!Strings.isNullOrEmpty(ip)) {
				addresses.add(InetAddresses.forString(ip));
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

		return addresses;
	}

	public List<Tag> buildAddressTags() {
		List<Tag> tags = Lists.newArrayList();

		List<String> publicIps = GoogleComputeClient.findPublicIps(instance);
		for (String ip : publicIps) {
			InetAddress addr = InetAddresses.forString(ip);
			tags.add(Tag.NETWORK_ADDRESS.build(addr));
		}

		return tags;
	}

	public GoogleCloud getCloud() {
		return cloud;
	}

	// public Instance getInstance() {
	// return instanceState;
	// }

	@Override
	public boolean isTerminated() {
		if (terminated) {
			return true;
		}

		InstanceState state = InstanceState.get(instance);
		log.debug("isTerminated? State=" + state);
		// if (state.isTerminated()) {
		// return true;
		// }
		//
		// if (state.isTerminating()) {
		// // TODO: Not sure if this is right
		// log.warn("isTerminated mapping isTerminating => isTerminated=true");
		// return true;
		// }

		return false;
	}

	public String getServerSelfLink() {
		return instance.getSelfLink();
	}

}
