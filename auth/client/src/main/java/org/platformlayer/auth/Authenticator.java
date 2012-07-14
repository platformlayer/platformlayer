package org.platformlayer.auth;

import java.io.PrintStream;

import org.platformlayer.model.AuthenticationToken;

public interface Authenticator {
	AuthenticationToken getAuthenticationToken() throws PlatformlayerAuthenticationException;

	void clearAuthenticationToken();

	String getHost();

	void setDebug(PrintStream debug);
}
