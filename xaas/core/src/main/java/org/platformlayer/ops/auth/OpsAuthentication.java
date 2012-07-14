package org.platformlayer.ops.auth;

import java.util.Map;

import javax.crypto.SecretKey;

import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.ProjectAuthorization;

import com.google.common.collect.Maps;

@Deprecated
public class OpsAuthentication {
	final Authentication auth;
	final OpsUser user;

	// final OpsProject project;

	public OpsAuthentication(Authentication auth, OpsUser user/* , OpsProject project */) {
		this.auth = auth;
		this.user = user;
		// this.project = project;
	}

	public String getUserKey() {
		return auth.getUserKey();
	}

	public SecretKey findUserSecret() {
		byte[] userSecret = auth.getUserSecret();
		if (userSecret == null) {
			return null;
		}
		return AesUtils.deserializeKey(userSecret);
	}

	final Map<ProjectId, ProjectAuthorization> projects = Maps.newHashMap();

	public ProjectAuthorization getProject(ProjectId projectId, AuthenticationTokenValidator tokenValidator) {
		ProjectAuthorization authz = projects.get(projectId);
		if (authz == null) {
			authz = tokenValidator.validate(auth.getToken(), projectId.getKey());
			if (authz == null) {
				throw new SecurityException();
			} else {
				projects.put(projectId, authz);
			}
		}

		return authz;
	}

	// public ProjectId getProjectId() {
	// return new ProjectId(project.getName());
	// }
	//
	// public boolean isInRole(ProjectId project, RoleId role) {
	// if (!project.equals(getProjectId())) {
	// return false;
	// }
	// if (role == RoleId.READ) {
	// // Everyone has read
	// return true;
	// }
	// // return roles.contains(role.getKey());
	// return false;
	// }
	//
	// public OpsProject getProject() {
	// return project;
	// }

	// @Override
	// public String toString() {
	// return "OpsAuthentication [user=" + user + ", project=" + project + "]";
	// }

	// public OpsUser getUser() {
	// return user;
	// }

	// public int getUserId() {
	// if (user == null)
	// return 0;
	//
	// return user.id;
	// }

}
