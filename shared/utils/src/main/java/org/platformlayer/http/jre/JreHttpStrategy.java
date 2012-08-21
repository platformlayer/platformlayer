package org.platformlayer.http.jre;

import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;

public class JreHttpStrategy implements HttpStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(JreHttpStrategy.class);

	@Override
	public HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		return new JreHttpConfiguration(sslConfiguration);
	}

}
