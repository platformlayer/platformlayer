package org.platformlayer.ops;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.networks.NetworkPoint;

import com.google.common.base.Objects;

public class OpaqueMachine extends MachineBase {
	final NetworkPoint address;

	public OpaqueMachine(NetworkPoint address) {
		this.address = address;
	}

	@Override
	public void terminate() throws OpsException {
		throw new UnsupportedOperationException();
	}

	// @Override
	// public String getServerId() {
	// return "raw:" + address;
	// }

	@Override
	public PlatformLayerKey getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String findAddress(NetworkPoint src, int destinationPort) {
		// TODO: Refactor this logic
		if (Objects.equal(address.getPrivateNetworkId(), src.getPrivateNetworkId())) {
			return address.getAddress().getHostAddress();
		}
		if (address.isPublicInternet()) {
			return address.getAddress().getHostAddress();
		}
		return null;
	}
}
