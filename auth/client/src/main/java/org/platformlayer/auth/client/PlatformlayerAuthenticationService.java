package org.platformlayer.auth.client;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.inject.Inject;

import org.platformlayer.auth.AuthenticationService;
import org.platformlayer.auth.PlatformlayerAuthenticationException;
import org.platformlayer.auth.v1.AuthenticateResponse;
import org.platformlayer.auth.v1.PasswordCredentials;

public class PlatformlayerAuthenticationService implements AuthenticationService {
	@Inject
	PlatformlayerAuthenticationClient keystoneUserClient;

	// @Inject
	// AuthenticationTokenValidator keystoneSystemClient;

	// @Override
	// public OpsProject findProject(OpsUser user, String projectKey) throws RepositoryException {
	// KeystoneUser keystoneUser = (KeystoneUser) user;
	// KeystoneAuthentication auth = keystoneUser.getAuth();
	//
	// if (projectKey.equals(auth.getProject().getName())) {
	// return new KeystoneProject(keystoneUser, auth.getProject());
	// }
	//
	// log.warn("Project did not match in keystone auth");
	//
	// return null;
	// }

	// @Override
	// public CertificateAuthenticationResponse authenticateWithCertificate(CertificateAuthenticationRequest request)
	// throws RepositoryException {
	// String username = request.username;
	// PrivateKey privateKey = request.privateKey;
	// X509Certificate[] certificateChain = request.certificateChain;
	//
	// if (privateKey == null || certificateChain == null) {
	// throw new IllegalArgumentException();
	// }
	//
	// // TODO: Cache auth tokens??
	// KeystoneAuthenticationToken authToken;
	// try {
	// authToken = keystoneUserClient.authenticateWithCertificate(username, certificateChain, privateKey);
	// } catch (KeystoneAuthenticationException e) {
	// throw new RepositoryException("Error authenticating", e);
	// }
	//
	// // // TODO: Cache decoded tokens?
	// // KeystoneAuthentication auth = (KeystoneAuthentication) keystoneSystemClient.validate(
	// // authToken.getAuthTokenValue(), projectKey);
	// // if (auth == null) {
	// // return null;
	// // }
	//
	// CertificateAuthenticationResponse response = new CertificateAuthenticationResponse();
	// response.user = new KeystoneUser(authToken);
	// return response;
	// }

	@Override
	public PlatformlayerAuthenticationToken authenticateWithCertificate(String username, PrivateKey privateKey,
			X509Certificate[] certificateChain) throws PlatformlayerAuthenticationException {
		if (username == null || privateKey == null || certificateChain == null) {
			throw new IllegalArgumentException();
		}

		// TODO: Cache auth tokens??
		PlatformlayerAuthenticationToken authToken = keystoneUserClient.authenticateWithCertificate(username,
				certificateChain, privateKey);

		return authToken;
	}

	@Override
	public PlatformlayerAuthenticationToken authenticateWithPassword(String username, String password)
			throws PlatformlayerAuthenticationException {
		PasswordCredentials passwordCredentials = new PasswordCredentials();
		passwordCredentials.setUsername(username);
		passwordCredentials.setPassword(password);

		// TODO: Cache auth tokens??
		AuthenticateResponse response = keystoneUserClient.authenticate(passwordCredentials);
		PlatformlayerAuthenticationToken authToken = new PlatformlayerAuthenticationToken(response.getAccess());

		return authToken;

		// // TODO: Cache decoded tokens?
		// KeystoneAuthentication auth = (KeystoneAuthentication) keystoneSystemClient.validate(
		// authToken.getAuthTokenValue(), project);
		// if (auth == null) {
		// return null;
		// }
		//
		// return new KeystoneUser(auth);
	}

}
