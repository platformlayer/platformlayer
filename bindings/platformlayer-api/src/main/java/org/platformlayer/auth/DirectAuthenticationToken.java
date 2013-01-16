package org.platformlayer.auth;

import org.platformlayer.http.HttpRequest;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.fathomdb.utils.Base64;

public class DirectAuthenticationToken implements AuthenticationToken {
	public static final String PREFIX = "project:";

	private final String token;
	private final CryptoKey secret;

	public DirectAuthenticationToken(String token, CryptoKey secret) {
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
		httpRequest.setRequestHeader("X-Auth-Secret", Base64.encode(FathomdbCrypto.serialize(secret)));
	}

	public String getToken() {
		return token;
	}

	public CryptoKey getSecret() {
		return secret;
	}

}
