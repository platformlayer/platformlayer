package org.platformlayer;

import java.net.URI;

public class PlatformLayerClientNotFoundException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	private final URI url;

	public PlatformLayerClientNotFoundException(String message, int httpCode, URI url) {
		super(message, httpCode);
		this.url = url;
	}

	public URI getUrl() {
		return url;
	}

}
