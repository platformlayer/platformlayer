package org.platformlayer.ops.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.log4j.Logger;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.ops.OpsException;

public class KeyStoreEncryptionStore implements EncryptionStore {
	private static final Logger log = Logger.getLogger(KeyStoreEncryptionStore.class);

	private static final String DEFAULT_PASSWORD = "notasecret";

	final KeyStore keyStore;

	public KeyStoreEncryptionStore(KeyStore keyStore) {
		super();
		this.keyStore = keyStore;
	}

	@Override
	public CertificateAndKey getCertificateAndKey(String alias) throws OpsException {
		String password = DEFAULT_PASSWORD;

		// TODO: Cache??

		CertificateAndKey certificateAndKey;
		try {
			certificateAndKey = KeyStoreUtils.getCertificateAndKey(keyStore, alias, password);
		} catch (GeneralSecurityException e) {
			throw new OpsException("Error reading private key", e);
		}

		if (certificateAndKey == null) {
			log.warn("Unable to find private key: " + alias);
			throw new OpsException("Private key not found");
		}

		return certificateAndKey;
	}
}
