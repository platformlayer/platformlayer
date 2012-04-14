package org.platformlayer.ssh.mina;

import java.net.SocketAddress;
import java.security.PublicKey;

import org.apache.sshd.ClientSession;
import org.apache.sshd.client.ServerKeyVerifier;
import org.platformlayer.ops.ssh.IServerKeyVerifier;

public class ServerKeyVerifierAdapter implements ServerKeyVerifier {

	private final IServerKeyVerifier serverKeyVerifier;

	public ServerKeyVerifierAdapter(IServerKeyVerifier serverKeyVerifier) {
		this.serverKeyVerifier = serverKeyVerifier;
	}

	@Override
	public boolean verifyServerKey(ClientSession sshClientSession, SocketAddress remoteAddress, PublicKey serverKey) {
		return serverKeyVerifier.verifyServerKey(remoteAddress, serverKey);
	}

}
