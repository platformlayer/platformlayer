package org.platformlayer.ops.ssh;

import java.net.SocketAddress;
import java.security.PublicKey;

public interface IServerKeyVerifier {
	boolean verifyServerKey(SocketAddress remoteAddress, PublicKey serverKey);

	void verifyPooled(IServerKeyVerifier serverKeyVerifier);
}
