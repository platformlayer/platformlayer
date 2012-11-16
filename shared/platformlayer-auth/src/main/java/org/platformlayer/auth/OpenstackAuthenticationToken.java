package org.platformlayer.auth;


public interface OpenstackAuthenticationToken extends AuthenticationToken {
	String getServiceUrl(String serviceKey);
}
