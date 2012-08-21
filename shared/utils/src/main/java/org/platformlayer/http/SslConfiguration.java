package org.platformlayer.http;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SslConfiguration {
	public static final SslConfiguration EMPTY = new SslConfiguration(null, null, null);

	final KeyManager keyManager;
	final TrustManager trustManager;
	final HostnameVerifier hostnameVerifier;

	public SslConfiguration(KeyManager keyManager, TrustManager trustManager, HostnameVerifier hostnameVerifier) {
		this.keyManager = keyManager;
		this.trustManager = trustManager;
		this.hostnameVerifier = hostnameVerifier;
	}

	public TrustManager getTrustManager() {
		return trustManager;
	}

	public HostnameVerifier getHostnameVerifier() {
		return hostnameVerifier;
	}

	public KeyManager getKeyManager() {
		return keyManager;
	}

	public SslConfiguration copyWithNewKeyManager(KeyManager newKeyManager) {
		return new SslConfiguration(newKeyManager, trustManager, hostnameVerifier);
	}

	public SSLSocketFactory getSslSocketFactory() throws UnrecoverableKeyException, KeyManagementException,
			NoSuchAlgorithmException, KeyStoreException {

		if (getTrustManager() != null || getKeyManager() != null) {
			SSLSocketFactory sslSocketFactory = SslHelpers.buildSslSocketFactory(getKeyManager(), getTrustManager());
			return sslSocketFactory;
		} else {
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		}
	}

	public boolean isEmpty() {
		return keyManager == null && trustManager == null && hostnameVerifier == null;
	}

}
