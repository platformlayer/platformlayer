package org.platformlayer.xaas;

import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;
import org.openstack.crypto.KeyStoreUtils;

public class ClientCertificateKeyManager implements X509KeyManager {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ClientCertificateKeyManager.class);

	private final KeyStore keystore;

	private final String keystorePassword;

	public ClientCertificateKeyManager(KeyStore keystore, String keystorePassword) {
		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		try {
			List<String> aliases = KeyStoreUtils.getAliases(keystore);
			if (aliases.size() == 1) {
				return aliases.get(0);
			}
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException("Error choosing client certificate", e);
		}

		throw new UnsupportedOperationException();
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
		try {
			Certificate[] certificateChain = keystore.getCertificateChain(alias);
			X509Certificate[] x509chain = new X509Certificate[certificateChain.length];
			for (int i = 0; i < certificateChain.length; i++) {
				x509chain[i] = (X509Certificate) certificateChain[i];
			}
			return x509chain;
		} catch (KeyStoreException e) {
			throw new IllegalArgumentException("Error getting client certificate", e);
		}
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		try {
			Key key = keystore.getKey(alias, keystorePassword.toCharArray());
			return (PrivateKey) key;
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error getting client certificate", e);
		}
	}
}
