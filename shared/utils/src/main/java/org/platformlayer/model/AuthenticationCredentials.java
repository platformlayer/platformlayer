package org.platformlayer.model;

import org.platformlayer.auth.AuthenticationToken;

public interface AuthenticationCredentials {
	AuthenticationToken getToken();
}
