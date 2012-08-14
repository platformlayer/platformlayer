package org.platformlayer.auth;

import java.security.cert.X509Certificate;

import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;

public interface AuthenticationTokenValidator {
	ProjectAuthorization validateToken(AuthenticationToken auth, String projectKey);

	ProjectAuthorization validateChain(X509Certificate[] chain, String projectKey);
}
