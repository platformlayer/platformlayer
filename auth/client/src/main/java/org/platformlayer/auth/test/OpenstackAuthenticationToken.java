package org.platformlayer.auth.test;

import java.util.Map;

public class OpenstackAuthenticationToken {
	private final String authToken;
	private final Map<String, String> allHeaders;

	public OpenstackAuthenticationToken(String authToken, Map<String, String> allHeaders) {
		this.authToken = authToken;
		this.allHeaders = allHeaders;
	}

	public String getHeaderValue(String key) {
		return allHeaders.get(key);
	}

	public String getAuthTokenValue() {
		return authToken;
	}

}