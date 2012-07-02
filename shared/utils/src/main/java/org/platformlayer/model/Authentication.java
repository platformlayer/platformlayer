package org.platformlayer.model;

public interface Authentication {
	ProjectAuthentication getProject();

	String getUserKey();

	// TODO: Remove
	@Deprecated
	boolean isInRole(String project, RoleId role);

	byte[] getUserSecret();
}
