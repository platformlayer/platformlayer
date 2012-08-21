package org.platformlayer.http.apache;

import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;

public class ApacheCommonsHttpStrategy implements HttpStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ApacheCommonsHttpStrategy.class);

	@Override
	public HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		return new ApacheCommonsHttpConfiguration(sslConfiguration);
	}

}
