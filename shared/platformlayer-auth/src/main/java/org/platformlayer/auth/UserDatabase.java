package org.platformlayer.auth;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.crypto.SecretKey;

import org.openstack.keystone.services.ServiceAccount;
import org.platformlayer.RepositoryException;

public interface UserDatabase extends UserRepository {
	OpsUser createUser(String userName, String password) throws RepositoryException;

	OpsUser findUser(String userName) throws RepositoryException;

	OpsUser findUserById(int userId) throws RepositoryException;

	List<OpsProject> listProjectsByUserId(int userId) throws RepositoryException;

	void addUserToProject(String username, String projectKey, SecretKey projectSecret) throws RepositoryException;

	void grantProjectToProject(String grantToProjectKey, String onProjectKey, SecretKey onProjectSecret)
			throws RepositoryException;

	List<String> listAllUserNames(String prefix) throws RepositoryException;

	OpsProject findProjectByKey(String key) throws RepositoryException;

	OpsProject createProject(String key, OpsUser owner) throws RepositoryException;

	List<String> listAllProjectNames(String prefix) throws RepositoryException;

	ServiceAccount findServiceAccount(String name, byte[] publicKey) throws RepositoryException;

	ServiceAccount createServiceAccount(X509Certificate cert) throws RepositoryException;
}
