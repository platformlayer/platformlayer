package org.platformlayer.auth.system;

import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.model.Authentication;

public class PlatformlayerUserAuthentication implements Authentication {
	final AuthenticationToken authToken;

	private final String userKey;

	public PlatformlayerUserAuthentication(AuthenticationToken authToken, String userKey) {
		this.authToken = authToken;
		this.userKey = userKey;
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

	// @Override
	// public byte[] getUserSecret() {
	// return userSecret;
	// }

	@Override
	public String getUserKey() {
		return userKey;
	}

	@Override
	public AuthenticationToken getToken() {
		return authToken;
	}

}
