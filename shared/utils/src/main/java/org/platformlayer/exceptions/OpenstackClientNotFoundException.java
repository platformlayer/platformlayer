package org.platformlayer.exceptions;

import java.net.URL;

import org.platformlayer.PlatformLayerClientException;

public class OpenstackClientNotFoundException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	private final URL url;

	public OpenstackClientNotFoundException(String message, URL url) {
		super(message);
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}
}
