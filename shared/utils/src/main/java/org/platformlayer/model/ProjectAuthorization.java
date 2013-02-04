package org.platformlayer.model;

import java.util.List;

import org.platformlayer.auth.ProjectInfo;

public interface ProjectAuthorization extends ProjectInfo {
	Authentication getUser();

	List<RoleId> getRoles();

}
