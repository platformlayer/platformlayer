package org.platformlayer.http.apache;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.SslConfiguration;

@Singleton
public class ApacheCommonsHttpStrategy extends CachingHttpStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ApacheCommonsHttpStrategy.class);

	@Override
	protected HttpConfiguration build0(SslConfiguration sslConfiguration) {
		log.info("Build http configuration for: " + sslConfiguration);
		return new ApacheCommonsHttpConfiguration(sslConfiguration);
	}
}
