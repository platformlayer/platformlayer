package org.openstack.keystone.service;

import java.util.List;

import org.platformlayer.model.Authentication;
import org.platformlayer.model.RoleId;

public class KeystoneAuthentication implements Authentication {
	private final String userKey;
	private final String tenantKey;
	private final List<String> roles;
	private final byte[] userSecret;

	public KeystoneAuthentication(String userKey, String tenantKey, byte[] userSecret, List<String> roles) {
		this.userKey = userKey;
		this.tenantKey = tenantKey;
		this.userSecret = userSecret;
		this.roles = roles;
	}

	@Override
	public String getProject() {
		return tenantKey;
	}

	@Override
	public boolean isInRole(String project, RoleId role) {
		if (project.equals(tenantKey)) {
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
