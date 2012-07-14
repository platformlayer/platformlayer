package org.platformlayer.auth;

import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;

public interface AuthenticationTokenValidator {
	ProjectAuthorization validate(AuthenticationToken auth, String projectKey);
}
