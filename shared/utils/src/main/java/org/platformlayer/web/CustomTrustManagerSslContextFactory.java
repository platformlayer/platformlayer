package org.platformlayer.web;

import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;

import javax.net.ssl.TrustManager;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTrustManagerSslContextFactory extends SslContextFactory {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CustomTrustManagerSslContextFactory.class);
	private TrustManager[] trustManagers;

	public CustomTrustManagerSslContextFactory() {
		super(SslContextFactory.DEFAULT_KEYSTORE_PATH);
	}

	public void setTrustManagers(TrustManager[] trustManagers) {
		this.trustManagers = trustManagers;
	}

	@Override
	protected TrustManager[] getTrustManagers(KeyStore trustStore, Collection<? extends CRL> crls) throws Exception {
		if (trustManagers != null) {
			return trustManagers;
		}
		return super.getTrustManagers(trustStore, crls);
	}

}
