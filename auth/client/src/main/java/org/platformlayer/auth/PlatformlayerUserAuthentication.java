package org.platformlayer.auth;

import org.platformlayer.model.Authentication;
import org.platformlayer.model.AuthenticationToken;

public class PlatformlayerUserAuthentication implements Authentication {
	final AuthenticationToken authToken;

	private final String userKey;
	private final byte[] userSecret;

	public PlatformlayerUserAuthentication(AuthenticationToken authToken, String userKey, byte[] userSecret) {
		this.authToken = authToken;
		this.userKey = userKey;
		this.userSecret = userSecret;
	}

	// @Override
	// public boolean isInRole(String projectKey, RoleId role) {
	// if (projectKey.equals(project.getName())) {
	// if (role == RoleId.READ) {
	// // Everyone has read
	// return true;
	// }
	//
	// return roles.contains(role.getKey());
	// }
	// return false;
	// }

	@Override
	public byte[] getUserSecret() {
		return userSecret;
	}

	@Override
	public String getUserKey() {
		return userKey;
	}

	@Override
	public AuthenticationToken getToken() {
		return authToken;
	}

}
