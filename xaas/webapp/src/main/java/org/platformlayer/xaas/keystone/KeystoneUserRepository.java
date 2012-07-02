package org.platformlayer.xaas.keystone;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.openstack.keystone.service.KeystoneAuthentication;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.UserRepository;

public class KeystoneUserRepository implements UserRepository {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KeystoneUserRepository.class);

	@Override
	public OpsUser authenticateWithPassword(String username, String password) throws RepositoryException {
		throw new UnsupportedOperationException();
	}

	@Override
	public OpsProject authenticateProject(String projectKey, SecretKey secret) throws RepositoryException {
		throw new UnsupportedOperationException();
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
