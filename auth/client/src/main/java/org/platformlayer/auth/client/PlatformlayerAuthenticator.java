package org.platformlayer.auth.client;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.PlatformlayerAuthenticationException;
import org.platformlayer.auth.v1.PasswordCredentials;
import org.platformlayer.crypto.AcceptAllHostnameVerifier;
import org.platformlayer.crypto.PublicKeyTrustManager;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.http.SslConfiguration;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.rest.JreRestfulClient;
import org.platformlayer.rest.RestfulClient;

public class PlatformlayerAuthenticator implements Authenticator {
	final String username;
	final String password;

	final PlatformlayerAuthenticationClient client;

	AuthenticationToken token = null;

	public PlatformlayerAuthenticator(HttpStrategy httpStrategy, String username, String password, String baseUrl,
			List<String> trustKeys) {
		this.username = username;
		this.password = password;

		HostnameVerifier hostnameVerifier = null;
		KeyManager keyManager = null;
		TrustManager trustManager = null;

		if (trustKeys != null) {
			trustManager = new PublicKeyTrustManager(trustKeys);

			hostnameVerifier = new AcceptAllHostnameVerifier();
		}

		SslConfiguration sslConfiguration = new SslConfiguration(keyManager, trustManager, hostnameVerifier);
		RestfulClient restfulClient = new JreRestfulClient(httpStrategy, baseUrl, sslConfiguration);
		this.client = new PlatformlayerAuthenticationClient(restfulClient);
	}

	@Override
	public AuthenticationToken getAuthenticationToken() throws PlatformlayerAuthenticationException {
		if (token == null) {
			PasswordCredentials passwordCredentials = new PasswordCredentials();
			passwordCredentials.setUsername(username);
			passwordCredentials.setPassword(password);

			token = client.authenticate(passwordCredentials);
		}
		return token;
	}

	@Override
	public void clearAuthenticationToken() {
		token = null;
	}

	@Override
	public String getHost() {
		URI url = client.getBaseUri();
		return url.getHost();
	}

	@Override
	public void setDebug(PrintStream debug) {
		client.setDebug(debug);
	}

}
