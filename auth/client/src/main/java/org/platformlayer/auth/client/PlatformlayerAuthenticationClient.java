package org.platformlayer.auth.client;

import java.io.PrintStream;
import java.net.URI;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.KeyManager;

import org.platformlayer.auth.PlatformlayerAuthenticationException;
import org.platformlayer.auth.v1.Auth;
import org.platformlayer.auth.v1.AuthenticateRequest;
import org.platformlayer.auth.v1.AuthenticateResponse;
import org.platformlayer.auth.v1.CertificateCredentials;
import org.platformlayer.auth.v1.PasswordCredentials;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.crypto.SimpleClientCertificateKeyManager;
import org.platformlayer.rest.RestClientException;
import org.platformlayer.rest.RestfulClient;
import org.platformlayer.rest.RestfulRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformlayerAuthenticationClient {
	static final Logger log = LoggerFactory.getLogger(PlatformlayerAuthenticationClient.class);

	public static final String AUTH_HEADER = "X-Auth-Token";

	public static final Integer HTTP_500_ERROR = new Integer(500);

	protected static final int MAX_RETRIES = 10;

	static Random random = new Random();

	final RestfulClient httpClient;

	public PlatformlayerAuthenticationClient(RestfulClient httpClient) {
		this.httpClient = httpClient;
	}

	public PlatformlayerAuthenticationToken authenticate(PasswordCredentials passwordCredentials)
			throws PlatformlayerAuthenticationException {
		Auth auth = new Auth();
		auth.setPasswordCredentials(passwordCredentials);

		AuthenticateRequest request = new AuthenticateRequest();
		request.setAuth(auth);

		AuthenticateResponse response;
		try {
			response = doSimpleRequest("POST", "tokens", request, AuthenticateResponse.class);
		} catch (RestClientException e) {
			throw new PlatformlayerAuthenticationException("Error authenticating", e);
		}

		// if (log.isDebugEnabled()) {
		// if (response.getAccess().getProjects() != null) {
		// for (String project : response.getAccess().getProjects()) {
		// log.debug("Can access: " + project);
		// }
		// }
		// }

		return new PlatformlayerAuthenticationToken(response.getAccess());
	}

	public PlatformlayerAuthenticationToken authenticateWithCertificate(String username,
			X509Certificate[] certificateChain, PrivateKey privateKey) throws PlatformlayerAuthenticationException {
		if (username == null) {
			throw new IllegalArgumentException();
		}

		CertificateCredentials certificateCredentials = new CertificateCredentials();
		certificateCredentials.setUsername(username);

		Auth auth = new Auth();
		auth.setCertificateCredentials(certificateCredentials);

		AuthenticateRequest request = new AuthenticateRequest();
		request.setAuth(auth);

		final KeyManager keyManager = new SimpleClientCertificateKeyManager(privateKey, certificateChain);

		for (int i = 0; i < 2; i++) {
			AuthenticateResponse response;
			try {
				RestfulRequest<AuthenticateResponse> httpRequest = httpClient.buildRequest("POST", "tokens", request,
						AuthenticateResponse.class);

				httpRequest.setKeyManager(keyManager);

				response = httpRequest.execute();
			} catch (RestClientException e) {
				throw new PlatformlayerAuthenticationException("Error authenticating", e);
			}

			if (i == 0) {
				if (response == null || response.getChallenge() == null) {
					return null;
				}

				byte[] challenge = response.getChallenge();
				byte[] challengeResponse = RsaUtils.decrypt(privateKey, challenge);
				certificateCredentials.setChallengeResponse(challengeResponse);
			} else {
				if (response == null || response.getAccess() == null) {
					return null;
				}
				return new PlatformlayerAuthenticationToken(response.getAccess());
			}
		}

		return null;
	}

	protected <T> T doSimpleRequest(String method, String relativeUri, Object postObject, Class<T> responseClass)
			throws RestClientException {
		RestfulRequest<T> request = httpClient.buildRequest(method, relativeUri, postObject, responseClass);
		return request.execute();
	}

	public URI getBaseUri() {
		return httpClient.getBaseUri();
	}

	public void setDebug(PrintStream debug) {
		httpClient.setDebug(debug);
	}

}
