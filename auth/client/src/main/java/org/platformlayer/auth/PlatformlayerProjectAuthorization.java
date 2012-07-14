package org.platformlayer.auth;

import javax.crypto.SecretKey;

import org.platformlayer.auth.v1.ProjectValidation;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthorization;

public class PlatformlayerProjectAuthorization implements ProjectAuthorization {

	final PlatformlayerUserAuthentication user;
	final ProjectValidation project;

	public PlatformlayerProjectAuthorization(PlatformlayerUserAuthentication user, ProjectValidation project) {
		this.user = user;
		this.project = project;
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
	public SecretKey getProjectSecret() {
		return AesUtils.deserializeKey(project.getSecret());
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public int getId() {
		return Integer.parseInt(project.getId());
	}

	@Override
	public Authentication getUser() {
		return user;
	}

	@Override
	public boolean isLocked() {
		return false;
	}

}
