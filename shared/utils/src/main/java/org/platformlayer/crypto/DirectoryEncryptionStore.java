package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.SimpleCertificateAndKey;
import com.fathomdb.crypto.bouncycastle.PrivateKeys;
import com.google.common.base.Preconditions;

public class DirectoryEncryptionStore implements EncryptionStore {
	private File base;

	public DirectoryEncryptionStore(File base) {
		this.base = base;
	}

	@Override
	public CertificateAndKey getCertificateAndKey(String alias) {
		CertificateAndKey certificateAndKey;

		Preconditions.checkNotNull(alias);

		// Path to file
		File certPath = new File(base, alias + ".crt");

		List<X509Certificate> certificate;
		try {
			certificate = CertificateUtils.fromPem(certPath);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading certificate: " + certPath, e);
		}

		File keyPath = new File(base, alias + ".key");

		PrivateKey privateKey;
		try {
			privateKey = PrivateKeys.fromPem(keyPath);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading private key: " + keyPath, e);
		}

		certificateAndKey = new SimpleCertificateAndKey(certificate, privateKey);

		return certificateAndKey;
	}

}
