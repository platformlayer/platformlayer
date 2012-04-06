package org.openstack.keystone.services;

public class UserAuthenticatorAdaptor implements UserAuthenticator {
    final GenericAuthenticator authenticator;

    public UserAuthenticatorAdaptor(GenericAuthenticator authenticator) {
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