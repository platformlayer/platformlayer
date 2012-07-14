package org.platformlayer.auth;

import org.platformlayer.PlatformLayerClientException;

public class PlatformlayerAuthenticationException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	public PlatformlayerAuthenticationException(String message) {
		super(message);
	}

	public PlatformlayerAuthenticationException(String message, Exception e) {
		super(message, e);
	}
}
