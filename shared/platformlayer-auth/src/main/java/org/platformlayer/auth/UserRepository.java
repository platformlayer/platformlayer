package org.platformlayer.auth;

import java.util.List;

import javax.crypto.SecretKey;

import org.platformlayer.RepositoryException;

public interface UserRepository {
	OpsUser createUser(String userName, String password) throws RepositoryException;

	OpsUser findUser(String userName) throws RepositoryException;

	OpsUser findUserById(int userId) throws RepositoryException;

	List<OpsProject> listProjectsByUserId(int userId) throws RepositoryException;

	// OpsUser findUserForToken(int userId, byte[] tokenSecret) throws RepositoryException;

	void addUserToProject(String username, String projectKey, SecretKey projectSecret) throws RepositoryException;

	void grantProjectToProject(String grantToProjectKey, String onProjectKey, SecretKey onProjectSecret)
			throws RepositoryException;

	List<String> listAllUserNames(String prefix) throws RepositoryException;

	OpsProject findProjectByKey(String key) throws RepositoryException;

	OpsProject createProject(String key, OpsUser owner) throws RepositoryException;

	List<String> listAllProjectNames(String prefix) throws RepositoryException;

	// ItemBase getManagedItem(ModelKey modelKey, boolean fetchTags) throws RepositoryException;
	//
	// Tags changeTags(ModelClass<?> modelClass, ProjectId projectId, ManagedItemId itemId, TagChanges changeTags)
	// throws RepositoryException;
	//
	// <T extends ItemBase> T createManagedItem(ProjectId project, T item) throws RepositoryException;
	//
	// <T extends ItemBase> T updateManagedItem(ProjectId project, T item) throws RepositoryException;
	//
	// <T> void changeState(ProjectId projectId, ItemBase item, ManagedItemState newState) throws RepositoryException;
	//
	// <T extends ItemBase> List<T> findAll(ModelClass<T> modelClass, ProjectId project, boolean fetchTags) throws
	// RepositoryException;
}
