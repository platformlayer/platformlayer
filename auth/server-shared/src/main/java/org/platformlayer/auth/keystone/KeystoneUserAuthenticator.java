package org.platformlayer.auth.keystone;

import org.openstack.keystone.services.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

public interface KeystoneUserAuthenticator {
	UserEntity authenticate(String project, String username, String password) throws AuthenticatorException;

	ProjectEntity findProject(String projectKey, OpsUser user) throws AuthenticatorException;

	UserEntity getUserFromToken(String userId, byte[] tokenSecret) throws AuthenticatorException;

	CertificateAuthenticationResponse authenticate(CertificateAuthenticationRequest request)
			throws AuthenticatorException;
}
