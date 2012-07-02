package org.platformlayer.xaas.keystone;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.docs.identity.api.v2.PasswordCredentials;
import org.openstack.keystone.auth.client.KeystoneAuthenticationClient;
import org.openstack.keystone.auth.client.KeystoneAuthenticationException;
import org.openstack.keystone.auth.client.KeystoneAuthenticationToken;
import org.openstack.keystone.service.AuthenticationTokenValidator;
import org.openstack.keystone.service.KeystoneAuthentication;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;

public class KeystoneUserRepository implements UserRepository {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KeystoneUserRepository.class);

	@Inject
	KeystoneAuthenticationClient keystoneUserClient;

	@Inject
	AuthenticationTokenValidator keystoneSystemClient;

	@Override
	public OpsUser authenticateWithPassword(String project, String username, String password)
			throws RepositoryException {
		PasswordCredentials passwordCredentials = new PasswordCredentials();
		passwordCredentials.setUsername(username);
		passwordCredentials.setPassword(password);

		// TODO: Cache auth tokens??
		KeystoneAuthenticationToken authToken;
		try {
			authToken = keystoneUserClient.authenticate(project, passwordCredentials);
		} catch (KeystoneAuthenticationException e) {
			throw new RepositoryException("Error authenticating", e);
		}

		// TODO: Cache decoded tokens?
		KeystoneAuthentication auth = (KeystoneAuthentication) keystoneSystemClient.validate(authToken
				.getAuthTokenValue());
		if (auth == null) {
			return null;
		}

		return new KeystoneUser(auth);
	}

	@Override
	public OpsProject findProject(OpsUser user, String projectKey) throws RepositoryException {
		KeystoneUser keystoneUser = (KeystoneUser) user;
		KeystoneAuthentication auth = keystoneUser.getAuth();

		if (projectKey.equals(auth.getProject().getName())) {
			return new KeystoneProject(keystoneUser, auth.getProject());
		}

		log.warn("Project did not match in keystone auth");

		return null;
	}
}