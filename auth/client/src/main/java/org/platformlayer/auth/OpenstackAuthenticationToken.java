package org.platformlayer.auth;

import org.platformlayer.model.AuthenticationToken;

public interface OpenstackAuthenticationToken extends AuthenticationToken {
	String getServiceUrl(String serviceKey);
}
