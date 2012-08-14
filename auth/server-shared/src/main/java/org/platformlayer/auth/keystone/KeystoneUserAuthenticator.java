package org.platformlayer.auth.keystone;

import java.util.List;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.UserProjectEntity;
import org.platformlayer.auth.model.CertificateChainInfo;

public interface KeystoneUserAuthenticator {
	UserEntity authenticate(String username, String password) throws AuthenticatorException;

	ProjectEntity findProject(String projectKey) throws AuthenticatorException;

	UserEntity getUserFromToken(String userId, byte[] tokenSecret) throws AuthenticatorException;

	CertificateAuthenticationResponse authenticate(CertificateAuthenticationRequest request)
			throws AuthenticatorException;

	List<ProjectEntity> listProjects(UserEntity user) throws RepositoryException;

	UserEntity findUserFromKeychain(CertificateChainInfo chain, boolean unlock) throws AuthenticatorException;

	UserProjectEntity findUserProject(UserEntity user, ProjectEntity project) throws AuthenticatorException;
}
