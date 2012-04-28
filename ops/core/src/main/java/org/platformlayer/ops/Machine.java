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

	public abstract String findAddress(NetworkPoint src, int destinationPort);

	public String getAddress(NetworkPoint src, int destinationPort) throws OpsException {
		String address = findAddress(src, destinationPort);
		if (address == null) {
			throw new OpsException("Cannot determine appropriate network address");
		}
		return address;
	}

}
