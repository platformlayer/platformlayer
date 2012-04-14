package org.platformlayer.ops.ssh;

import java.net.SocketAddress;
import java.security.PublicKey;

public class AcceptAllServerKeyVerifier implements IServerKeyVerifier {

	/**
	 * Use AcceptAllLearningServerKeyVerifier instead...
	 */
	protected AcceptAllServerKeyVerifier() {
	}

	@Override
	public boolean verifyServerKey(SocketAddress remoteAddress, PublicKey serverKey) {
		return true;
	}

	@Override
	public void verifyPooled(IServerKeyVerifier serverKeyVerifier) {
	}

}
