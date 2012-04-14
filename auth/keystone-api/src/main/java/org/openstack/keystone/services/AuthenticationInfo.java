package org.openstack.keystone.services;

public class AuthenticationInfo {
	final String userId;
	final byte[] tokenSecret;

	public AuthenticationInfo(String userId, byte[] tokenSecret) {
		this.userId = userId;
		this.tokenSecret = tokenSecret;
	}

	public String getUserId() {
		return userId;
	}

	public byte[] getTokenSecret() {
		return tokenSecret;
	}
}
