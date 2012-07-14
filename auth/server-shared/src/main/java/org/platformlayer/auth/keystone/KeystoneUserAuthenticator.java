package org.platformlayer.auth.keystone;

import java.util.List;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

public interface KeystoneUserAuthenticator {
	UserEntity authenticate(String username, String password) throws AuthenticatorException;

	ProjectEntity findProject(String projectKey, OpsUser user) throws AuthenticatorException;

	UserEntity getUserFromToken(String userId, byte[] tokenSecret) throws AuthenticatorException;

	CertificateAuthenticationResponse authenticate(CertificateAuthenticationRequest request)
			throws AuthenticatorException;

	List<ProjectEntity> listProjects(UserEntity user) throws RepositoryException;
}
