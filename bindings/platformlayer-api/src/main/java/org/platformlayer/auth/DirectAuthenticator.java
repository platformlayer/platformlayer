package org.platformlayer.auth;

import java.io.PrintStream;

public class DirectAuthenticator implements Authenticator {
	final DirectAuthenticationToken authenticationToken;

	public DirectAuthenticator(DirectAuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	@Override
	public DirectAuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}

	@Override
	public void clearAuthenticationToken() {
		// We can't do anything useful here
	}

	@Override
	public String getHost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDebug(PrintStream debug) {

	}

}
