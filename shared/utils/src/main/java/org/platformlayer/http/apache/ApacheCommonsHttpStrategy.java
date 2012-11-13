package org.platformlayer.http.apache;

import javax.inject.Singleton;

import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.SslConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApacheCommonsHttpStrategy extends CachingHttpStrategy {
	private static final Logger log = LoggerFactory.getLogger(ApacheCommonsHttpStrategy.class);

	@Override
	protected HttpConfiguration build0(SslConfiguration sslConfiguration) {
		log.info("Build http configuration for: " + sslConfiguration);
		return new ApacheCommonsHttpConfiguration(sslConfiguration);
	}
}
