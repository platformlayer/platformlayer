package org.platformlayer.auth.client;

import java.io.PrintStream;
import java.net.URI;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.platformlayer.WellKnownPorts;
import org.platformlayer.auth.PlatformlayerAuthenticationException;
import org.platformlayer.auth.v1.Auth;
import org.platformlayer.auth.v1.AuthenticateRequest;
import org.platformlayer.auth.v1.AuthenticateResponse;
import org.platformlayer.auth.v1.CertificateCredentials;
import org.platformlayer.auth.v1.PasswordCredentials;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.crypto.SimpleClientCertificateKeyManager;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.rest.HttpPayload;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestClientException;
import org.platformlayer.rest.RestfulClient;
import org.platformlayer.rest.RestfulRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

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

	public static class Provider implements javax.inject.Provider<PlatformlayerAuthenticationClient> {
		@Inject
		Configuration configuration;

		@Inject
		HttpStrategy httpStrategy;

		@Override
		public PlatformlayerAuthenticationClient get() {
			String keystoneUserUrl = configuration.lookup("auth.user.url", "https://127.0.0.1:"
					+ WellKnownPorts.PORT_PLATFORMLAYER_AUTH_USER + "/v2.0/");

			HostnameVerifier hostnameVerifier = null;

			KeyManager keyManager = null;

			TrustManager trustManager = null;

			String trustKeys = configuration.lookup("auth.user.ssl.keys", null);

			if (trustKeys != null) {
				trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

				hostnameVerifier = new AcceptAllHostnameVerifier();
			}

			SslConfiguration sslConfiguration = new SslConfiguration(keyManager, trustManager, hostnameVerifier);
			RestfulClient restfulClient = new JreRestfulClient(httpStrategy, keystoneUserUrl, sslConfiguration);
			PlatformlayerAuthenticationClient authClient = new PlatformlayerAuthenticationClient(restfulClient);

			return authClient;
		}
	}

	public AuthenticateResponse authenticate(PasswordCredentials passwordCredentials)
			throws PlatformlayerAuthenticationException {
		Auth auth = new Auth();
		auth.setPasswordCredentials(passwordCredentials);

		AuthenticateRequest request = new AuthenticateRequest();
		request.setAuth(auth);

		AuthenticateResponse response;
		try {
			response = doSimpleXmlRequest("POST", "tokens", request, AuthenticateResponse.class);
		} catch (RestClientException e) {
			Integer httpResponseCode = e.getHttpResponseCode();
			if (httpResponseCode != null && httpResponseCode == 401) {
				throw new PlatformlayerInvalidCredentialsException("Invalid credentials");
			}

			throw new PlatformlayerAuthenticationException("Error authenticating", e);
		}

		// if (log.isDebugEnabled()) {
		// if (response.getAccess().getProjects() != null) {
		// for (String project : response.getAccess().getProjects()) {
		// log.debug("Can access: " + project);
		// }
		// }
		// }

		return response;
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
				RestfulRequest<AuthenticateResponse> httpRequest = httpClient.buildRequest("POST", "tokens",
						HttpPayload.asXml(request), AuthenticateResponse.class);

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

	protected <T> T doSimpleXmlRequest(String method, String relativeUri, Object postObject, Class<T> responseClass)
			throws RestClientException {
		HttpPayload payload = postObject != null ? HttpPayload.asXml(postObject) : null;
		RestfulRequest<T> request = httpClient.buildRequest(method, relativeUri, payload, responseClass);
		return request.execute();
	}

	public URI getBaseUri() {
		return httpClient.getBaseUri();
	}

	public void setDebug(PrintStream debug) {
		httpClient.setDebug(debug);
	}

}
