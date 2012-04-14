package org.platformlayer.ops.ssh;

import java.net.SocketAddress;
import java.security.PublicKey;

public class RequiredServerKeyVerifier implements IServerKeyVerifier {
	private final PublicKey requiredServerKey;

	public RequiredServerKeyVerifier(PublicKey requiredServerKey) {
		super();
		this.requiredServerKey = requiredServerKey;
	}

	@Override
	public boolean verifyServerKey(SocketAddress remoteAddress, PublicKey serverKey) {
		return requiredServerKey.equals(serverKey);
	}

	@Override
	public void verifyPooled(IServerKeyVerifier serverKeyVerifier) {
	}
}
