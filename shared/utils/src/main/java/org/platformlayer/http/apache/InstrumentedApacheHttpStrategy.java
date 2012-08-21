package org.platformlayer.http.apache;

import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.SslConfiguration;

public class InstrumentedApacheHttpStrategy extends ApacheCommonsHttpStrategy {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(InstrumentedApacheHttpStrategy.class);

	@Override
	public HttpConfiguration buildConfiguration(SslConfiguration sslConfiguration) {
		return new InstrumentedApacheCommonsHttpConfiguration(sslConfiguration);
	}

}
