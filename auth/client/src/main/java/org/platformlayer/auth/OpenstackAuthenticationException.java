package org.platformlayer.auth;

import org.platformlayer.PlatformLayerClientException;

public class OpenstackAuthenticationException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	public OpenstackAuthenticationException(String message) {
		super(message);
	}

	public OpenstackAuthenticationException(String message, Exception e) {
		super(message, e);
	}
}
