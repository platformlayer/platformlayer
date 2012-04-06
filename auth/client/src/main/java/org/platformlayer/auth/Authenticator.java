package org.platformlayer.auth;

public interface Authenticator {
    AuthenticationToken getAuthenticationToken() throws OpenstackAuthenticationException;

    void clearAuthenticationToken();

    String getHost();
}
