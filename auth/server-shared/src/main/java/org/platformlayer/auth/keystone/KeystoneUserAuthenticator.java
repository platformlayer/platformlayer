package org.platformlayer.auth.keystone;

import org.openstack.keystone.services.AuthenticatorException;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

public interface KeystoneUserAuthenticator {
	UserEntity authenticate(String username, String password) throws AuthenticatorException;

	// byte[] getUserSecret(String userIdString, byte[] tokenSecret) throws AuthenticatorException;

	// GroupMembershipOracle getGroupMembership();

	ProjectEntity findProject(String projectKey, OpsUser user) throws AuthenticatorException;

	UserEntity getUserFromToken(String userId, byte[] tokenSecret) throws AuthenticatorException;
}
