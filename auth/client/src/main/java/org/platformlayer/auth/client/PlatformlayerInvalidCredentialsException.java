package org.platformlayer.auth.client;

import org.platformlayer.auth.PlatformlayerAuthenticationException;

public class PlatformlayerInvalidCredentialsException extends PlatformlayerAuthenticationException {
	private static final long serialVersionUID = 1L;

	public PlatformlayerInvalidCredentialsException(String message) {
		super(message);
	}

}
