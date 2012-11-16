package org.platformlayer.auth.client;

import java.io.PrintStream;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.crypto.Cipher;
import javax.net.ssl.KeyManager;

import org.platformlayer.auth.PlatformlayerAuthenticationClientException;
import org.platformlayer.auth.PlatformlayerAuthenticationToken;
import org.platformlayer.auth.PlatformlayerInvalidCredentialsException;
import org.platformlayer.auth.v1.Auth;
import org.platformlayer.auth.v1.AuthenticateRequest;
import org.platformlayer.auth.v1.AuthenticateResponse;
import org.platformlayer.auth.v1.CertificateCredentials;
import org.platformlayer.auth.v1.PasswordCredentials;
import org.platformlayer.rest.HttpPayload;
import org.platformlayer.rest.RestClientException;
import org.platformlayer.rest.RestfulClient;
import org.platformlayer.rest.RestfulRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.SimpleClientCertificateKeyManager;

public class PlatformLayerAuthenticationClient {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerAuthenticationClient.class);

	public static final String AUTH_HEADER = "X-Auth-Token";

	public static final Integer HTTP_500_ERROR = new Integer(500);

	protected static final int MAX_RETRIES = 10;

	static Random random = new Random();

	final RestfulClient httpClient;

	public PlatformLayerAuthenticationClient(RestfulClient httpClient) {
		this.httpClient = httpClient;
	}

	public AuthenticateResponse authenticate(PasswordCredentials passwordCredentials)
			throws PlatformlayerAuthenticationClientException {
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

			throw new PlatformlayerAuthenticationClientException("Error authenticating", e);
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
			X509Certificate[] certificateChain, PrivateKey privateKey)
			throws PlatformlayerAuthenticationClientException {
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
				throw new PlatformlayerAuthenticationClientException("Error authenticating", e);
			}

			if (i == 0) {
				if (response == null || response.getChallenge() == null) {
					return null;
				}

				byte[] challenge = response.getChallenge();
				byte[] challengeResponse = decrypt(privateKey, challenge);
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

	public static byte[] decrypt(PrivateKey key, byte[] cipherText) {
		try {
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] plainText = cipher.doFinal(cipherText);
			return plainText;
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error in decryption", e);
		}
	}

	public static void initDecrypt(Cipher cipher, Key key) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException("Invalid key", e);
		}
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
