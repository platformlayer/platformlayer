package org.platformlayer.auth.keystone;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.keystone.services.AuthenticatorException;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;

public class KeystoneRepositoryAuthenticator implements KeystoneUserAuthenticator {
	private static final Logger log = Logger.getLogger(KeystoneRepositoryAuthenticator.class);

	@Inject
	UserDatabase repository;

	@Override
	public UserEntity authenticate(String project, String username, String password) throws AuthenticatorException {
		if (username == null || password == null) {
			return null;
		}

		UserEntity user;
		try {
			user = (UserEntity) repository.authenticateWithPassword(project, username, password);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		if (user == null) {
			return null;
		}

		return user;

		// String userKey = "" + user.id;
		// byte[] tokenSecret = user.getTokenSecret();
		// AuthenticationInfo authentication = new AuthenticationInfo(userKey, tokenSecret);
		// return authentication;
	}

	@Override
	public CertificateAuthenticationResponse authenticate(CertificateAuthenticationRequest request)
			throws AuthenticatorException {
		try {
			return repository.authenticateWithCertificate(request);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}
	}

	// @Override
	// public byte[] getUserSecret(String userIdString, byte[] tokenSecret) throws AuthenticatorException {
	// int userId;
	// try {
	// userId = Integer.parseInt(userIdString);
	// } catch (NumberFormatException e) {
	// throw new AuthenticatorException("Invalid user id", e);
	// }
	//
	// UserEntity user;
	// try {
	// user = (UserEntity) repository.findUserById(userId);
	// } catch (RepositoryException e) {
	// throw new AuthenticatorException("Error while authenticating user", e);
	// }
	//
	// if (user != null) {
	// user.unlockWithToken(UserEntity.TOKEN_ID_DEFAULT, tokenSecret);
	// // TODO: Verify the item secret somehow??
	// }
	//
	// if (user == null) {
	// throw new AuthenticatorException("User not found");
	// }
	//
	// SecretKey secretKey = user.getUserSecret();
	// if (secretKey != null) {
	// return secretKey.getEncoded();
	// }
	// return null;
	// }

	// class SqlGroupMembershipOracle implements GroupMembershipOracle {
	// @Override
	// public List<String> getGroups(String key, boolean isGroup) throws AuthenticatorException {
	// if (!isGroup) {
	// int userId = Integer.parseInt(key);
	// List<OpsProject> groups;
	// try {
	// groups = repository.listProjectsByUserId(userId);
	// } catch (RepositoryException e) {
	// throw new AuthenticatorException("Error while listing user groups", e);
	// }
	// List<String> groupIds = Lists.newArrayList();
	// for (OpsProject group : groups) {
	// groupIds.add(group.getName());
	// }
	// return groupIds;
	// } else {
	// return Collections.emptyList();
	// }
	// }
	//
	// };

	// @Override
	// public GroupMembershipOracle getGroupMembership() {
	// return new SqlGroupMembershipOracle();
	// }

	@Override
	public ProjectEntity findProject(String projectKey, final OpsUser user) throws AuthenticatorException {
		ProjectEntity project;
		try {
			project = (ProjectEntity) repository.findProjectByKey(projectKey);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while fetching project", e);
		}
		if (project == null) {
			return null;
		}

		// int userId = Integer.parseInt(auth.getUserKey());
		// user = userRepository.findUserById(userId);
		// if (user == null) {
		// log.warn("User not found: " + userId);
		// throw new SecurityException();
		// }

		// byte[] userSecret = auth.getUserSecret();
		// if (userSecret == null) {
		// throw new SecurityException();
		// }
		//
		// user.unlock(AesUtils.deserializeKey(userSecret));

		project.unlockWithUser(user);

		if (!project.isSecretValid()) {
			return null;
		}

		return project;
	}

	@Override
	public UserEntity getUserFromToken(String userIdString, byte[] tokenSecret) throws AuthenticatorException {
		int userId;
		try {
			userId = Integer.parseInt(userIdString);
		} catch (NumberFormatException e) {
			throw new AuthenticatorException("Invalid user id", e);
		}

		UserEntity user;
		try {
			user = (UserEntity) repository.findUserById(userId);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		user.unlockWithToken(UserEntity.TOKEN_ID_DEFAULT, tokenSecret);

		if (user.isLocked()) {
			return null;
		}

		return user;
	}
}
