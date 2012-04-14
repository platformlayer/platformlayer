package org.platformlayer;

public class PlatformLayerAuthenticationException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	public PlatformLayerAuthenticationException(String message) {
		super(message);
	}

	public PlatformLayerAuthenticationException(String message, Exception e) {
		super(message, e);
	}
}
