package org.platformlayer.http.jre;

import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JreHttpStrategy implements HttpStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(JreHttpStrategy.class);

	@Override
	public HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		return new JreHttpConfiguration(sslConfiguration);
	}

}
