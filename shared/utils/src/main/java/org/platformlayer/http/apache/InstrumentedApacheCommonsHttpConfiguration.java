package org.platformlayer.http.apache;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.SchemeRegistryFactory;
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
		if (schemeRegistry == null) {
			schemeRegistry = SchemeRegistryFactory.createDefault();
		}

		return new InstrumentedClientConnManager(schemeRegistry);
	}

	@Override
	protected HttpClient buildDefaultHttpClient(ClientConnectionManager connectionManager, HttpParams httpParams) {
		HttpClient httpClient = new InstrumentedHttpClient((InstrumentedClientConnManager) connectionManager,
				httpParams);
		return httpClient;
	}

	private static HttpClient sharedHttpClient;

	@Override
	protected HttpClient getSharedHttpClient() {
		if (sharedHttpClient == null) {
			synchronized (InstrumentedApacheCommonsHttpConfiguration.class) {
				ClientConnectionManager connectionManager = buildConnectionManager(null);
				HttpParams httpParams = null;
				HttpClient httpClient = buildDefaultHttpClient(connectionManager, httpParams);
				sharedHttpClient = wrapHttpClient(httpClient);
			}
		}

		return sharedHttpClient;
	}
}