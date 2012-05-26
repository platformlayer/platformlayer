package org.platformlayer.auth;

import java.io.PrintStream;

public interface Authenticator {
	AuthenticationToken getAuthenticationToken() throws OpenstackAuthenticationException;

	void clearAuthenticationToken();

	String getHost();

	void setDebug(PrintStream debug);
}
