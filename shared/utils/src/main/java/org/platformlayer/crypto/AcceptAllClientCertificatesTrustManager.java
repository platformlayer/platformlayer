package org.platformlayer.crypto;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

public class AcceptAllClientCertificatesTrustManager implements X509TrustManager {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AcceptAllClientCertificatesTrustManager.class);

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// Accept all!
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
