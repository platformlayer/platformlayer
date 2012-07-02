package org.platformlayer.auth.keystone;

import java.util.Collections;
import java.util.List;

import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.services.AuthenticationInfo;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.GroupMembershipOracle;
import org.openstack.keystone.services.UserAuthenticator;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;

import com.google.common.collect.Lists;

public class KeystoneOpsAuthenticator implements UserAuthenticator {
	private static final Logger log = Logger.getLogger(KeystoneOpsAuthenticator.class);

	@Inject
	UserRepository repository;

	@Override
	public AuthenticationInfo authenticate(String username, String password) throws AuthenticatorException {
		OpsUser user;
		try {
			user = repository.findUser(username);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		if (user != null) {
			if (!user.isPasswordMatch(password)) {
				user = null;
			}
		}

		if (user == null) {
			return null;
		}

		user.unlockWithPassword(password);

		String userKey = "" + user.id;

		byte[] tokenSecret = user.getTokenSecret();
		AuthenticationInfo authentication = new AuthenticationInfo(userKey, tokenSecret);
		return authentication;
	}

	@Override
	public byte[] getUserSecret(String userIdString, byte[] tokenSecret) throws AuthenticatorException {
		int userId;
		try {
			userId = Integer.parseInt(userIdString);
		} catch (NumberFormatException e) {
			throw new AuthenticatorException("Invalid user id", e);
		}

		OpsUser user;
		try {
			user = repository.findUserById(userId);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		if (user != null) {
			user.unlockWithToken(OpsUser.TOKEN_ID_DEFAULT, tokenSecret);
			// TODO: Verify the item secret somehow??
		}

		if (user == null) {
			throw new AuthenticatorException("User not found");
		}

		SecretKey secretKey = user.getUserSecret();
		if (secretKey != null) {
			return secretKey.getEncoded();
		}
		return null;
	}

	class SqlGroupMembershipOracle implements GroupMembershipOracle {
		@Override
		public List<String> getGroups(String key, boolean isGroup) throws AuthenticatorException {
			if (!isGroup) {
				int userId = Integer.parseInt(key);
				List<OpsProject> groups;
				try {
					groups = repository.listProjectsByUserId(userId);
				} catch (RepositoryException e) {
					throw new AuthenticatorException("Error while listing user groups", e);
				}
				List<String> groupIds = Lists.newArrayList();
				for (OpsProject group : groups) {
					groupIds.add(group.key);
				}
				return groupIds;
			} else {
				return Collections.emptyList();
			}
		}

	};

	@Override
	public GroupMembershipOracle getGroupMembership() {
		return new SqlGroupMembershipOracle();
	}

}
