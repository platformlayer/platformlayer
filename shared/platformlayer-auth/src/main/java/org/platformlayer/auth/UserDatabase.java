package org.platformlayer.auth;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.crypto.SecretKey;

import org.openstack.crypto.CertificateAndKey;
import org.platformlayer.RepositoryException;
import org.platformlayer.model.RoleId;
import org.platformlayer.ops.OpsException;

public interface UserDatabase extends UserRepository {
	UserEntity createUser(String userName, String password, Certificate[] certificateChain) throws RepositoryException;

	UserEntity findUser(String userName) throws RepositoryException;

	UserEntity findUserById(int userId) throws RepositoryException;

	UserEntity findUserByPublicKey(byte[] publicKeyHash) throws RepositoryException;

	List<ProjectEntity> listProjectsByUserId(int userId) throws RepositoryException;

	void addUserToProject(String username, String projectKey, SecretKey projectSecret, List<RoleId> roles)
			throws RepositoryException;

	void grantProjectToProject(String grantToProjectKey, String onProjectKey, SecretKey onProjectSecret)
			throws RepositoryException;

	List<String> listAllUserNames(String prefix) throws RepositoryException;

	ProjectEntity findProjectByKey(String key) throws RepositoryException;

	ProjectEntity createProject(String key, OpsUser owner) throws RepositoryException;

	List<String> listAllProjectNames(String prefix) throws RepositoryException;

	ServiceAccountEntity findServiceAccount(String name, byte[] publicKey) throws RepositoryException;

	ServiceAccount createServiceAccount(X509Certificate cert) throws RepositoryException;

	UserProjectEntity findUserProject(int userId, int projectId) throws RepositoryException;

	List<ServiceAccountEntity> listAllServiceAccounts(byte[] filterPublicKey) throws RepositoryException;

	CertificateAndKey getProjectPki(ProjectEntity project) throws RepositoryException, OpsException;
}
