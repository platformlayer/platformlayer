package org.openstack.keystone.services;

import java.io.Serializable;
import java.util.Collection;

public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public final String userId;
	public final String username;
	public final Collection<String> groups;
	public final String email;
	public final byte[] secret;

	public UserInfo(String userId, String username, String email, byte[] secret, Collection<String> groups) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.secret = secret;
		this.groups = groups;
	}

}
