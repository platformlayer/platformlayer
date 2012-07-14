package org.platformlayer.auth.server;

import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;

import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.ssl.SslContextFactory;

public class CustomTrustManagerSslContextFactory extends SslContextFactory {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CustomTrustManagerSslContextFactory.class);
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
