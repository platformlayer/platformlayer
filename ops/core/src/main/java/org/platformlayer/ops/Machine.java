package org.platformlayer.ops;

import java.security.KeyPair;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.networks.NetworkPoint;

public abstract class Machine {
	// TODO: Introduce state??
	public abstract boolean isTerminated();

	public abstract void terminate() throws OpsException;

	public abstract OpsTarget getTarget(String user, KeyPair sshKeyPair) throws OpsException;

	public OpsTarget getTarget(SshKey sshKey) throws OpsException {
		return getTarget(sshKey.getUser(), sshKey.getKeyPair());
	}

	public abstract PlatformLayerKey getKey();

	// public abstract List<InetAddress> findAddresses(NetworkPoint src, int destinationPort);
	//
	// public InetAddress getBestAddress(NetworkPoint src, int destinationPort, InetAddressChooser chooser)
	// throws OpsException {
	// List<InetAddress> addresses = findAddresses(src, destinationPort);
	// InetAddress address = chooser.choose(addresses);
	//
	// if (address == null) {
	// throw new OpsException("Cannot determine appropriate network address");
	// }
	// return address;
	// }
	//
	// public String getBestAddress(NetworkPoint src, int destinationPort) throws OpsException {
	// InetAddressChooser chooser;
	// if (!src.isPublicAddress()) {
	// chooser = InetAddressChooser.preferIpv6();
	// } else {
	// chooser = InetAddressChooser.preferIpv4();
	// }
	//
	// InetAddress address = getBestAddress(src, destinationPort, chooser);
	// if (address == null) {
	// return null;
	// }
	// return address.getHostAddress();
	// }

	public abstract NetworkPoint getNetworkPoint() throws OpsException;
}
