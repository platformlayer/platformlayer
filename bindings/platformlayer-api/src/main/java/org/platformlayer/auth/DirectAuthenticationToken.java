package org.platformlayer.auth;

import javax.crypto.SecretKey;

import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.model.AuthenticationToken;

public class DirectAuthenticationToken implements AuthenticationToken {
	public static final String PREFIX = "project:";

	private final String token;
	private final SecretKey secret;

	public DirectAuthenticationToken(String token, SecretKey secret) {
		this.token = token;
		this.secret = secret;
	}

	// @Override
	// public String getServiceUrl(String serviceKey) {
	// if (Objects.equal(HttpPlatformLayerClient.SERVICE_PLATFORMLAYER, serviceKey)) {
	// return serviceUrl;
	// }
	// return null;
	// }

	public static String encodeToken(int projectId, String projectName) {
		String token = PREFIX + projectId + ":" + projectName;
		return token;
	}

	@Override
	public void populateRequest(HttpRequest httpRequest) {
		httpRequest.setRequestHeader("X-Auth-Key", token);
		httpRequest.setRequestHeader("X-Auth-Secret", CryptoUtils.toBase64(secret.getEncoded()));
	}

	public String getToken() {
		return token;
	}

	public SecretKey getSecret() {
		return secret;
	}

}
