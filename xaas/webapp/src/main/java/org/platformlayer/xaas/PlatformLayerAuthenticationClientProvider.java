package org.platformlayer.xaas;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.platformlayer.auth.client.PlatformLayerAuthenticationClient;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestfulClient;

import com.fathomdb.Configuration;
import com.google.common.base.Splitter;

public class PlatformLayerAuthenticationClientProvider implements Provider<PlatformLayerAuthenticationClient> {
	private static final int PORT_PLATFORMLAYER_AUTH_USER = 5001;

	@Inject
	Configuration configuration;

	@Inject
	HttpStrategy httpStrategy;

	@Override
	public PlatformLayerAuthenticationClient get() {
		String keystoneUserUrl = configuration.lookup("auth.user.url", "https://127.0.0.1:"
				+ PORT_PLATFORMLAYER_AUTH_USER + "/v2.0/");

		HostnameVerifier hostnameVerifier = null;

		KeyManager keyManager = null;

		TrustManager trustManager = null;

		String trustKeys = configuration.lookup("auth.user.ssl.keys", null);

		if (trustKeys != null) {
			trustManager = new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys));

			hostnameVerifier = new AcceptAllHostnameVerifier();
		}

		SslConfiguration sslConfiguration = new SslConfiguration(keyManager, trustManager, hostnameVerifier);
		RestfulClient restfulClient = new JreRestfulClient(httpStrategy, keystoneUserUrl, sslConfiguration);
		PlatformLayerAuthenticationClient authClient = new PlatformLayerAuthenticationClient(restfulClient);

		return authClient;
	}
}
