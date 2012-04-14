package org.platformlayer.ops.auth;

import javax.crypto.SecretKey;

import org.platformlayer.auth.OpsProject;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.crypto.AesUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.RoleId;

public class OpsAuthentication {

	final Authentication auth;
	final OpsUser user;
	final OpsProject project;

	public OpsAuthentication(Authentication auth, OpsUser user, OpsProject project) {
		this.auth = auth;
		this.user = user;
		this.project = project;
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

	public ProjectId getProjectId() {
		return new ProjectId(auth.getProject());
	}

	public boolean isInRole(ProjectId project, RoleId role) {
		return auth.isInRole(project.getKey(), role);
	}

	public OpsProject getProject() {
		return project;
	}

	@Override
	public String toString() {
		return "OpsAuthentication [user=" + user + ", project=" + project + "]";
	}

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
