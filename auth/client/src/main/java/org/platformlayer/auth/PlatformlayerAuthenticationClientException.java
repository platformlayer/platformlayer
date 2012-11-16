package org.platformlayer.auth;

public class PlatformlayerAuthenticationClientException extends Exception {
	private static final long serialVersionUID = 1L;

	public PlatformlayerAuthenticationClientException(String message) {
		super(message);
	}

	public PlatformlayerAuthenticationClientException(String message, Exception e) {
		super(message, e);
	}
}
