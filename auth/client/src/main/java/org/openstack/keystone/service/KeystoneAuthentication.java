package org.openstack.keystone.service;

import java.util.List;

import javax.crypto.SecretKey;

import org.openstack.docs.identity.api.v2.ProjectValidation;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthentication;
import org.platformlayer.model.RoleId;

public class KeystoneAuthentication implements Authentication {
	private final String userKey;
	private final List<String> roles;
	private final byte[] userSecret;

	private final ProjectValidation project;

	public KeystoneAuthentication(String userKey, ProjectValidation project, byte[] userSecret, List<String> roles) {
		this.userKey = userKey;
		this.project = project;
		this.userSecret = userSecret;
		this.roles = roles;
	}

	@Override
	public ProjectAuthentication getProject() {
		return new ProjectAuthentication() {
			@Override
			public SecretKey getSecret() {
				return AesUtils.deserializeKey(project.getSecret());
			}

			@Override
			public String getName() {
				return project.getName();
			}

			@Override
			public String getId() {
				return project.getId();
			}
		};
	}

	@Override
	public boolean isInRole(String projectKey, RoleId role) {
		if (projectKey.equals(project.getName())) {
			if (role == RoleId.READ) {
				// Everyone has read
				return true;
			}

			return roles.contains(role.getKey());
		}
		return false;
	}

	@Override
	public byte[] getUserSecret() {
		return userSecret;
	}

	@Override
	public String getUserKey() {
		return userKey;
	}
}
