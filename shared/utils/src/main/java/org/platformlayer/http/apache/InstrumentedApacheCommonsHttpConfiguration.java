package org.platformlayer.http.apache;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.params.HttpParams;
import org.platformlayer.http.SslConfiguration;

import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;

class InstrumentedApacheCommonsHttpConfiguration extends ApacheCommonsHttpConfiguration {
	InstrumentedApacheCommonsHttpConfiguration(SslConfiguration sslConfiguration) {
		super(sslConfiguration);
	}

	@Override
	protected ClientConnectionManager buildConnectionManager(SchemeRegistry schemeRegistry) {
		return new InstrumentedClientConnManager(schemeRegistry);
	}

	@Override
	protected HttpClient buildDefaultHttpClient(ClientConnectionManager connectionManager, HttpParams httpParams) {
		HttpClient httpClient = new InstrumentedHttpClient((InstrumentedClientConnManager) connectionManager,
				httpParams);
		return httpClient;
	}
}