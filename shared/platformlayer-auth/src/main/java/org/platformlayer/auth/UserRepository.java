package org.platformlayer.auth;

import javax.crypto.SecretKey;

import org.platformlayer.RepositoryException;

public interface UserRepository {
	OpsUser authenticateWithPassword(String username, String password) throws RepositoryException;

	OpsProject authenticateProject(String projectKey, SecretKey secret) throws RepositoryException;

	OpsProject findProject(OpsUser user, String projectKey) throws RepositoryException;
}
