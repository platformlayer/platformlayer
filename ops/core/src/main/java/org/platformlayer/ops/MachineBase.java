package org.platformlayer.ops;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;

import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.ssh.AcceptAllLearningServerKeyVerifier;
import org.platformlayer.ops.ssh.IServerKeyVerifier;
import org.platformlayer.ops.ssh.ISshContext;
import org.platformlayer.ops.ssh.SshConnection;

public abstract class MachineBase extends Machine {
	@Override
	public OpsTarget getTarget(String user, KeyPair sshKeyPair) throws OpsException {
		OpsSystem opsSystem = OpsContext.get().getOpsSystem();
		ISshContext sshContext = opsSystem.getSshContext();

		SshConnection sshConnection = sshContext.getSshConnection(user);

		String address = getBestAddress(NetworkPoint.forMe(), 22);
		try {
			sshConnection.setHost(InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			throw new OpsException("Error resolving address: " + address, e);
		}

		sshConnection.setKeyPair(sshKeyPair);

		File tempDirBase = new File("/tmp/");

		// TODO: Verify the server key once we've learned it
		IServerKeyVerifier serverKeyVerifier = new AcceptAllLearningServerKeyVerifier();
		sshConnection.setServerKeyVerifier(serverKeyVerifier);
		return new SshOpsTarget(tempDirBase, sshConnection);
	}
}
