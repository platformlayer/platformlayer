package org.platformlayer.auth.keystone;

import java.util.List;

import javax.inject.Inject;

import org.openstack.utils.Hex;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserDatabase;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.UserProjectEntity;
import org.platformlayer.auth.model.CertificateChainInfo;
import org.platformlayer.metrics.Instrumented;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.CryptoKey;
import com.google.common.base.Strings;

@Instrumented
public class KeystoneRepositoryAuthenticator implements KeystoneUserAuthenticator {
	private static final Logger log = LoggerFactory.getLogger(KeystoneRepositoryAuthenticator.class);

	@Inject
	UserDatabase repository;

	@Inject
	AuthenticationSecrets authenticationSecrets;

	@Override
	public UserEntity authenticate(String username, String password) throws AuthenticatorException {
		if (username == null || password == null) {
			return null;
		}

		UserEntity user;
		try {
			user = (UserEntity) repository.authenticateWithPassword(username, password);
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
	public ProjectEntity findProject(String projectKey) throws AuthenticatorException {
		ProjectEntity project;
		try {
			project = repository.findProjectByKey(projectKey);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while fetching project", e);
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

		if (tokenSecret.length < 1) {
			throw new IllegalArgumentException();
		}

		CryptoKey userSecret = authenticationSecrets.decryptSecretFromToken(tokenSecret);
		if (userSecret == null) {
			throw new AuthenticatorException("Authentication timed out");
		}

		UserEntity user;
		try {
			user = repository.findUserById(userId);
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while authenticating user", e);
		}

		user.unlock(userSecret);

		// user.unlockWithToken(UserEntity.TOKEN_ID_DEFAULT, tokenSecret);

		if (user.isLocked()) {
			return null;
		}

		return user;
	}

	@Override
	public UserEntity findUserFromKeychain(CertificateChainInfo chain, boolean unlock) throws AuthenticatorException {
		if (chain.certificates == null || chain.certificates.isEmpty()) {
			return null;
		}

		for (int i = 0; i < chain.certificates.size(); i++) {
			String publicKeyHash = chain.certificates.get(i).publicKeyHash;
			if (Strings.isNullOrEmpty(publicKeyHash)) {
				continue;
			}

			log.debug("Checking publicKeyHash: " + publicKeyHash);

			byte[] hash = Hex.fromHex(publicKeyHash);

			UserEntity user;
			try {
				user = repository.findUserByPublicKey(hash);
			} catch (RepositoryException e) {
				throw new AuthenticatorException("Error while authenticating user", e);
			}

			if (user != null) {
				return user;
			}
		}

		return null;
	}

	@Override
	public List<ProjectEntity> listProjects(UserEntity user) throws RepositoryException {
		return repository.listProjectsByUserId(user.getId());
	}

	@Override
	public UserProjectEntity findUserProject(UserEntity user, ProjectEntity project) throws AuthenticatorException {
		try {
			return repository.findUserProject(user.getId(), project.getId());
		} catch (RepositoryException e) {
			throw new AuthenticatorException("Error while querying authentication store", e);
		}

	}
}
