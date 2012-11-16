package org.platformlayer.auth;


public class PlatformlayerInvalidCredentialsException extends PlatformlayerAuthenticationClientException {
	private static final long serialVersionUID = 1L;

	public PlatformlayerInvalidCredentialsException(String message) {
		super(message);
	}

}
