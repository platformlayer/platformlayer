package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.openstack.crypto.SimpleCertificateAndKey;
import org.platformlayer.config.Configuration;
import org.platformlayer.ops.OpsException;

public class KeyStoreEncryptionStore implements EncryptionStore {
	private static final Logger log = Logger.getLogger(KeyStoreEncryptionStore.class);

	private static final String DEFAULT_PASSWORD = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;

	final KeyStore keyStore;

	public KeyStoreEncryptionStore(KeyStore keyStore) {
		super();
		this.keyStore = keyStore;
	}

	@Override
	public CertificateAndKey getCertificateAndKey(String alias) throws OpsException {
		CertificateAndKey certificateAndKey;

		if (alias.startsWith("/")) {
			// Path to file
			File certPath = new File(alias + ".crt");

			List<X509Certificate> certificate;
			try {
				certificate = CertificateUtils.fromPem(certPath);
			} catch (IOException e) {
				throw new OpsException("Error reading certificate: " + certPath, e);
			}

			File keyPath = new File(alias + ".key");

			PrivateKey privateKey;
			try {
				privateKey = PrivateKeys.fromPem(keyPath);
			} catch (IOException e) {
				throw new OpsException("Error reading private key: " + keyPath, e);
			}

			certificateAndKey = new SimpleCertificateAndKey(certificate, privateKey);
		} else {
			String password = DEFAULT_PASSWORD;

			// TODO: Cache??

			try {
				certificateAndKey = KeyStoreUtils.getCertificateAndKey(keyStore, alias, password);
			} catch (GeneralSecurityException e) {
				throw new OpsException("Error reading private key", e);
			}

			if (certificateAndKey == null) {
				log.warn("Unable to find private key: " + alias);
				throw new OpsException("Private key not found");
			}
		}

		return certificateAndKey;
	}

	private static KeyStoreEncryptionStore build(File keystoreFile, String keystoreSecret) {
		if (!keystoreFile.exists()) {
			throw new IllegalStateException("Keystore not found: " + keystoreFile.getAbsolutePath());
		}

		KeyStore keyStore;
		try {
			keyStore = KeyStoreUtils.load(keystoreFile, keystoreSecret);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Error loading keystore", e);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading keystore", e);
		}

		return new KeyStoreEncryptionStore(keyStore);
	}

	public static EncryptionStore build(Configuration configuration) {
		File keystoreFile = configuration.lookupFile("keystore", "keystore.jks");
		String secret = configuration.lookup("keystore.password", KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);

		return build(keystoreFile, secret);
	}
}
