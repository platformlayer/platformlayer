package org.openstack.keystone.auth.client;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.openstack.docs.identity.api.v2.Auth;
import org.openstack.docs.identity.api.v2.AuthenticateRequest;
import org.openstack.docs.identity.api.v2.AuthenticateResponse;
import org.openstack.docs.identity.api.v2.CertificateCredentials;
import org.openstack.docs.identity.api.v2.PasswordCredentials;
import org.openstack.keystone.service.RestClientException;
import org.openstack.keystone.service.RestfulClient;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.crypto.SimpleClientCertificateKeyManager;
import org.platformlayer.http.SimpleHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeystoneAuthenticationClient extends RestfulClient {
	static final Logger log = LoggerFactory.getLogger(KeystoneAuthenticationClient.class);

	public static final Integer HTTP_500_ERROR = new Integer(500);

	protected static final int MAX_RETRIES = 10;

	static Random random = new Random();

	public KeystoneAuthenticationClient(String baseUrl) {
		this(baseUrl, null, null, null);
	}

	public KeystoneAuthenticationClient(String baseUrl, KeyManager keyManager, TrustManager trustManager,
			HostnameVerifier hostnameVerifier) {
		super(baseUrl, keyManager, trustManager, hostnameVerifier);
	}

	public KeystoneAuthenticationToken authenticate(String project, PasswordCredentials passwordCredentials)
			throws KeystoneAuthenticationException {
		Auth auth = new Auth();
		auth.setPasswordCredentials(passwordCredentials);
		auth.setProject(project);

		AuthenticateRequest request = new AuthenticateRequest();
		request.setAuth(auth);

		AuthenticateResponse response;
		try {
			response = doSimpleRequest("POST", "tokens", request, AuthenticateResponse.class);
		} catch (RestClientException e) {
			throw new KeystoneAuthenticationException("Error authenticating", e);
		}
		return new KeystoneAuthenticationToken(response.getAccess());
	}

	public KeystoneAuthenticationToken authenticateWithCertificate(String username, String projectKey,
			X509Certificate[] certificateChain, PrivateKey privateKey) throws KeystoneAuthenticationException {
		if (username == null || projectKey == null) {
			throw new IllegalArgumentException();
		}

		CertificateCredentials certificateCredentials = new CertificateCredentials();
		certificateCredentials.setUsername(username);

		Auth auth = new Auth();
		auth.setCertificateCredentials(certificateCredentials);
		auth.setProject(projectKey);

		AuthenticateRequest request = new AuthenticateRequest();
		request.setAuth(auth);

		final KeyManager keyManager = new SimpleClientCertificateKeyManager(privateKey, certificateChain);

		for (int i = 0; i < 2; i++) {
			AuthenticateResponse response;
			try {
				Request<AuthenticateResponse> httpRequest = new Request<AuthenticateResponse>("POST", "tokens",
						request, AuthenticateResponse.class) {

					@Override
					protected void addHeaders(SimpleHttpRequest httpRequest) {
						super.addHeaders(httpRequest);

						httpRequest.setKeyManager(keyManager);
					}
				};

				response = httpRequest.execute();
			} catch (RestClientException e) {
				throw new KeystoneAuthenticationException("Error authenticating", e);
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
				return new KeystoneAuthenticationToken(response.getAccess());
			}
		}

		return null;
	}
}
