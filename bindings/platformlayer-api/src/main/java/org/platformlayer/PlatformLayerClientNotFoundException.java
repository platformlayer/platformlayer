package org.platformlayer;

import java.net.URL;

public class PlatformLayerClientNotFoundException extends PlatformLayerClientException {
	private static final long serialVersionUID = 1L;

	private final URL url;

	public PlatformLayerClientNotFoundException(String message, int httpCode, URL url) {
		super(message, httpCode);
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}

}
