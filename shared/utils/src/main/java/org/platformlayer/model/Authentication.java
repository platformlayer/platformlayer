package org.platformlayer.model;

import org.platformlayer.auth.AuthenticationToken;

public interface Authentication {
	String getUserKey();

	// byte[] getUserSecret();

	AuthenticationToken getToken();

	// ProjectAuthentication getProject();

	// boolean isInRole(String projectKey, RoleId role);
}
