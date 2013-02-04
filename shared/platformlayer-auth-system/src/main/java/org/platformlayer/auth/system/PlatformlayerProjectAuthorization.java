package org.platformlayer.auth.system;

import java.util.List;

import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.model.RoleId;

import com.fathomdb.crypto.CryptoKey;

public class PlatformlayerProjectAuthorization implements ProjectAuthorization {

	final Authentication user;
	final String name;
	final CryptoKey projectSecret;
	final List<RoleId> roles;
	final int id;

	public PlatformlayerProjectAuthorization(Authentication user, String name, CryptoKey projectSecret,
			List<RoleId> roles, int id) {
		super();
		this.user = user;
		this.name = name;
		this.projectSecret = projectSecret;
		this.roles = roles;
		this.id = id;
	}

	@Override
	public CryptoKey getProjectSecret() {
		return projectSecret;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Authentication getUser() {
		return user;
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public List<RoleId> getRoles() {
		return roles;
	}

}
