package org.platformlayer.auth.keystone;

import org.openstack.keystone.services.AuthenticationInfo;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.GroupMembershipOracle;
import org.openstack.keystone.services.SystemAuthenticator;

import com.google.common.base.Objects;

public class KeystoneSystemAuthenticator implements SystemAuthenticator {

	@Override
	public AuthenticationInfo authenticate(String username, String password) throws AuthenticatorException {
		boolean valid = Objects.equal("admin", username) && Objects.equal("secret", password);
		if (!valid) {
			return null;
		}
		AuthenticationInfo auth = new AuthenticationInfo(username, null);
		return auth;
	}

	@Override
	public GroupMembershipOracle getGroupMembership() {
		return null;
	}

	@Override
	public byte[] getUserSecret(String userId, byte[] tokenSecret) throws AuthenticatorException {
		throw new IllegalStateException();
	}

}
