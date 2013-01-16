package org.platformlayer.web;

import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.PlatformlayerAuthenticationToken;
import org.platformlayer.model.AuthenticationCredentials;

public class PlatformLayerAuthenticationCredentials implements AuthenticationCredentials {

	final AuthenticationToken token;

	public PlatformLayerAuthenticationCredentials(AuthenticationToken token) {
		super();
		this.token = token;
	}

	public PlatformLayerAuthenticationCredentials(String authToken) {
		super();
		this.token = new PlatformlayerAuthenticationToken(authToken);
	}

	@Override
	public AuthenticationToken getToken() {
		return token;
	}

}
