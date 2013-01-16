package org.platformlayer.auth;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public interface AuthenticationService {
	AuthenticationToken authenticateWithCertificate(String username, PrivateKey privateKey,
			X509Certificate[] certificateChain) throws PlatformlayerAuthenticationClientException;

	AuthenticationToken authenticateWithPassword(String username, String password)
			throws PlatformlayerAuthenticationClientException;
}
