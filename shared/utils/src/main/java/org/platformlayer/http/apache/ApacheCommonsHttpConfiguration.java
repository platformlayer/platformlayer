package org.platformlayer.http.apache;

import java.net.URI;
import java.security.GeneralSecurityException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.platformlayer.http.HttpConfiguration;
import org.platformlayer.http.HttpRequest;
import org.platformlayer.http.SslConfiguration;

public class ApacheCommonsHttpConfiguration implements HttpConfiguration {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ApacheCommonsHttpConfiguration.class);

	final HttpClient httpClient;

	private static HttpClient sharedHttpClient;

	ApacheCommonsHttpConfiguration(SslConfiguration sslConfiguration) {
		super();
		this.httpClient = buildHttpClient(sslConfiguration);
	}

	@Override
	public HttpRequest buildRequest(String method, URI uri) {
		return new ApacheCommonsHttpRequest(httpClient, method, uri);
	}

	private HttpClient buildHttpClient(SslConfiguration sslConfiguration) {
		if (sslConfiguration == null || sslConfiguration.isEmpty()) {
			return getSharedHttpClient();
		}

		// Pool custom http clients?
		SchemeSocketFactory schemeSocketFactory;
		try {
			javax.net.ssl.SSLSocketFactory sslSocketFactory = sslConfiguration.getSslSocketFactory();

			X509HostnameVerifier apacheHostnameVerifier = null;
			if (sslConfiguration.getHostnameVerifier() != null) {
				apacheHostnameVerifier = new ApacheHostnameVerifierAdapter(sslConfiguration.getHostnameVerifier());
			}
			schemeSocketFactory = new SSLSocketFactory(sslSocketFactory, apacheHostnameVerifier);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error building SSL client", e);
		}

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 443, schemeSocketFactory));

		ClientConnectionManager connectionManager = buildConnectionManager(schemeRegistry);

		HttpParams httpParams = null;
		HttpClient httpClient = buildDefaultHttpClient(connectionManager, httpParams);
		httpClient = wrapHttpClient(httpClient);

		return httpClient;
	}

	protected ClientConnectionManager buildConnectionManager(SchemeRegistry schemeRegistry) {
		if (schemeRegistry == null) {
			schemeRegistry = SchemeRegistryFactory.createDefault();
		}
		return new PoolingClientConnectionManager(schemeRegistry);
	}

	protected HttpClient buildDefaultHttpClient(ClientConnectionManager connectionManager, HttpParams httpParams) {
		HttpClient httpClient = new DefaultHttpClient(connectionManager, httpParams);
		return httpClient;
	}

	protected HttpClient wrapHttpClient(HttpClient httpClient) {
		httpClient = new DecompressingHttpClient(httpClient);
		return httpClient;
	}

	protected HttpClient getSharedHttpClient() {
		if (sharedHttpClient == null) {
			synchronized (ApacheCommonsHttpConfiguration.class) {
				ClientConnectionManager connectionManager = buildConnectionManager(null);
				HttpParams httpParams = null;
				HttpClient httpClient = buildDefaultHttpClient(connectionManager, httpParams);
				sharedHttpClient = wrapHttpClient(httpClient);
			}
		}

		return sharedHttpClient;
	}

}
