package org.platformlayer.auth;

import org.platformlayer.RepositoryException;

public interface UserRepository {
	OpsUser authenticateWithPassword(String projectKey, String username, String password) throws RepositoryException;

	OpsProject findProject(OpsUser user, String projectKey) throws RepositoryException;
}
