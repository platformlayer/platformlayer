package org.openstack.keystone.services;


public interface GenericAuthenticator {
	AuthenticationInfo authenticate(String username, String password) throws AuthenticatorException;

	GroupMembershipOracle getGroupMembership();

	byte[] getUserSecret(String userId, byte[] tokenSecret) throws AuthenticatorException;
}
