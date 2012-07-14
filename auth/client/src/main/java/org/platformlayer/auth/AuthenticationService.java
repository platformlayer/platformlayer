package org.platformlayer.auth;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.platformlayer.model.AuthenticationToken;

public interface AuthenticationService {
	AuthenticationToken authenticateWithCertificate(String username, PrivateKey privateKey,
			X509Certificate[] certificateChain) throws PlatformlayerAuthenticationException;

	AuthenticationToken authenticateWithPassword(String username, String password)
			throws PlatformlayerAuthenticationException;
}
