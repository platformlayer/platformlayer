package org.platformlayer.web;

import java.security.cert.X509Certificate;

import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.model.AuthenticationCredentials;

public class CertificateAuthenticationCredentials implements AuthenticationCredentials {

	private final X509Certificate[] certChain;

	public CertificateAuthenticationCredentials(X509Certificate[] certChain) {
		this.certChain = certChain;
	}

	@Override
	public AuthenticationToken getToken() {
		return null;
	}

	public X509Certificate[] getCertChain() {
		return certChain;
	}

}
