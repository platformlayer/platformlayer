package org.platformlayer.auth;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class CertificateAuthenticationRequest {
	public String projectKey;
	public String username;
	public X509Certificate[] certificateChain;
	public byte[] challengeResponse;
	public PrivateKey privateKey;
}
