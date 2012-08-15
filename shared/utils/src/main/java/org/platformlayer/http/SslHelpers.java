package org.platformlayer.http;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SslHelpers {
	public static SSLSocketFactory buildSslSocketFactory(KeyManager keyManager, TrustManager trustManager)
			throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");

		KeyManager[] keyManagers = null;
		if (keyManager != null) {
			keyManagers = new KeyManager[] { keyManager };
		}

		TrustManager[] trustManagers = null;

		if (trustManager != null) {
			trustManagers = new TrustManager[] { trustManager };
		}

		context.init(keyManagers, trustManagers, new SecureRandom());

		return context.getSocketFactory();
	}
}
