package org.platformlayer.ops;

import java.net.InetAddress;
import java.util.List;

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
	public List<InetAddress> findAddresses(NetworkPoint src, int destinationPort) {
		return address.findReachableAddresses(src);
	}

	@Override
	public boolean isTerminated() {
		return false;
	}
}
