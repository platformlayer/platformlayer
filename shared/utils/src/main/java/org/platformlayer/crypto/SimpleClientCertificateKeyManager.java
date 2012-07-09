package org.platformlayer.crypto;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import org.openstack.crypto.CertificateAndKey;

public class SimpleClientCertificateKeyManager implements X509KeyManager {
	private static final String ALIAS = "cert";

	final PrivateKey privateKey;
	final X509Certificate[] certificateChain;

	public SimpleClientCertificateKeyManager(PrivateKey privateKey, X509Certificate[] certificateChain) {
		super();
		this.privateKey = privateKey;
		this.certificateChain = certificateChain;
	}

	public SimpleClientCertificateKeyManager(CertificateAndKey certificateAndKey) {
		this(certificateAndKey.getPrivateKey(), certificateAndKey.getCertificateChain());
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		return ALIAS;
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		if (!alias.equals(ALIAS)) {
			throw new IllegalArgumentException();
		}

		return certificateChain;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		if (!alias.equals(ALIAS)) {
			throw new IllegalArgumentException();
		}

		return privateKey;

	}
}
