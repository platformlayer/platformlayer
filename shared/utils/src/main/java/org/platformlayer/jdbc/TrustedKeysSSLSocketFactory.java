package org.platformlayer.jdbc;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.fathomdb.crypto.ssl.PublicKeyTrustManager;
import com.google.common.base.Splitter;

public class TrustedKeysSSLSocketFactory extends DelegatingSSLSocketFactory {
	private final TrustManager trustManager;

	public TrustedKeysSSLSocketFactory(TrustManager trustManager) {
		super(buildSSLSocketFactory(trustManager));

		this.trustManager = trustManager;
	}

	private static SSLSocketFactory buildSSLSocketFactory(TrustManager trustManager) {
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Cannot find TLS provider", e);
		}
		try {
			ctx.init(null, new TrustManager[] { trustManager }, null);
		} catch (KeyManagementException e) {
			throw new IllegalStateException("Error initializing TLS", e);
		}

		return ctx.getSocketFactory();
	}

	public TrustedKeysSSLSocketFactory(String trustKeys) {
		this(new PublicKeyTrustManager(Splitter.on(',').trimResults().split(trustKeys)));
	}

}
