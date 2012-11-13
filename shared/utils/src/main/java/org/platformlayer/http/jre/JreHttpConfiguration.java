package org.platformlayer.http.jre;

import java.io.IOException;
import java.net.URI;

import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.http.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JreHttpConfiguration implements HttpConfiguration {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(JreHttpConfiguration.class);

	final SslConfiguration sslConfiguration;

	JreHttpConfiguration(SslConfiguration sslConfiguration) {
		super();
		if (sslConfiguration == null) {
			sslConfiguration = SslConfiguration.EMPTY;
		}
		this.sslConfiguration = sslConfiguration;
	}

	@Override
	public HttpRequest buildRequest(String method, URI uri) {
		try {
			return new JreHttpRequest(method, uri, sslConfiguration);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error building http request", e);
		}
	}

	@Override
	public void close() throws IOException {
	}

}
