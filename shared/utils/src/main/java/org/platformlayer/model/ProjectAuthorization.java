package org.platformlayer.model;

import org.platformlayer.auth.ProjectInfo;

public interface ProjectAuthorization extends ProjectInfo {
	Authentication getUser();
}
