package org.platformlayer.auth;

import org.platformlayer.RepositoryException;

public interface UserRepository {
	OpsUser authenticateWithPassword(String username, String password) throws RepositoryException;

	ProjectInfo findProject(OpsUser user, String projectKey) throws RepositoryException;

	CertificateAuthenticationResponse authenticateWithCertificate(CertificateAuthenticationRequest request)
			throws RepositoryException;
}
