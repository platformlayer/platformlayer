package org.openstack.keystone.services;

public class SystemAuthenticatorAdaptor implements SystemAuthenticator {
	final GenericAuthenticator authenticator;

	public SystemAuthenticatorAdaptor(GenericAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@Override
	public AuthenticationInfo authenticate(String username, String password) throws AuthenticatorException {
		return authenticator.authenticate(username, password);
	}

	@Override
	public GroupMembershipOracle getGroupMembership() {
		return authenticator.getGroupMembership();
	}

	@Override
	public byte[] getUserSecret(String userId, byte[] tokenSecret) throws AuthenticatorException {
		return authenticator.getUserSecret(userId, tokenSecret);
	}
}