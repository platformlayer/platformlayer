package org.platformlayer.gwt.client;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.api.login.Authentication;

@Singleton
public class ApplicationState {

	private Authentication authentication;

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

}
