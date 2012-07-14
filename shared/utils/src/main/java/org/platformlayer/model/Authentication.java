package org.platformlayer.model;

public interface Authentication {
	String getUserKey();

	byte[] getUserSecret();

	AuthenticationToken getToken();

	// ProjectAuthentication getProject();

	// boolean isInRole(String projectKey, RoleId role);
}
