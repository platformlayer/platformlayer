package org.platformlayer.ops;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.networks.NetworkPoint;

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
	public boolean isTerminated() {
		return false;
	}

	@Override
	public NetworkPoint getNetworkPoint() {
		return address;
	}
}
