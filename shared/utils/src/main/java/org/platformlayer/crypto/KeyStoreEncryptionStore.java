package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.EncryptionStore;
import com.fathomdb.crypto.KeyStoreUtils;
import com.fathomdb.crypto.SimpleCertificateAndKey;
import com.fathomdb.crypto.bouncycastle.PrivateKeys;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class KeyStoreEncryptionStore implements EncryptionStore {
	private static final Logger log = LoggerFactory.getLogger(KeyStoreEncryptionStore.class);

	private static final String DEFAULT_PASSWORD = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;

	final KeyStore keyStore;

	public KeyStoreEncryptionStore(KeyStore keyStore) {
		super();
		this.keyStore = keyStore;
	}

	public X509Certificate[] getCertificate(String alias) {
		Certificate[] certificateChain;

		// TODO: Cache??

		try {
			certificateChain = keyStore.getCertificateChain(alias);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error reading certifificate", e);
		}

		if (certificateChain == null) {
			Certificate certificate;
			try {
				certificate = keyStore.getCertificate(alias);
			} catch (GeneralSecurityException e) {
				throw new IllegalArgumentException("Error reading certifificate", e);
			}

			if (certificate != null) {
				X509Certificate x509Certificate = (X509Certificate) certificate;
				if (!x509Certificate.getIssuerDN().equals(x509Certificate.getSubjectDN())) {
					throw new IllegalStateException();
				}

				return new X509Certificate[] { x509Certificate };
			}
		}

		if (certificateChain == null) {
			log.warn("Unable to find certifificatey: " + alias);
			throw new IllegalArgumentException("Certificate not found");
		}

		return toX509(certificateChain);
	}

	private static X509Certificate[] toX509(Certificate[] chain) {
		X509Certificate[] x = new X509Certificate[chain.length];
		for (int i = 0; i < chain.length; i++) {
			x[i] = (X509Certificate) chain[i];
		}
		return x;
	}

	@Override
	public CertificateAndKey getCertificateAndKey(String alias) {
		CertificateAndKey certificateAndKey;

		if (alias.startsWith("/")) {
			// Path to file
			File certPath = new File(alias + ".crt");

			List<X509Certificate> certificate;
			try {
				certificate = CertificateUtils.fromPem(certPath);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error reading certificate: " + certPath, e);
			}

			File keyPath = new File(alias + ".key");

			PrivateKey privateKey;
			try {
				privateKey = PrivateKeys.fromPem(keyPath);
			} catch (IOException e) {
				throw new IllegalArgumentException("Error reading private key: " + keyPath, e);
			}

			certificateAndKey = new SimpleCertificateAndKey(certificate, privateKey);
		} else {
			String password = DEFAULT_PASSWORD;

			// TODO: Cache??

			try {
				certificateAndKey = KeyStoreUtils.getCertificateAndKey(keyStore, alias, password);
			} catch (GeneralSecurityException e) {
				throw new IllegalArgumentException("Error reading private key", e);
			}

			if (certificateAndKey == null) {
				log.warn("Unable to find private key: " + alias);
				throw new IllegalArgumentException("Private key not found");
			}
		}

		return certificateAndKey;
	}

	public static KeyStoreEncryptionStore build(File keystoreFile, String keystoreSecret) {
		KeyStore keyStore;

		if (!keystoreFile.exists()) {
			throw new IllegalStateException("Keystore not found: " + keystoreFile.getAbsolutePath());
		} else {
			try {
				keyStore = KeyStoreUtils.load(keystoreFile, keystoreSecret);
			} catch (GeneralSecurityException e) {
				throw new IllegalStateException("Error loading keystore", e);
			} catch (IOException e) {
				throw new IllegalStateException("Error loading keystore", e);
			}
		}

		return new KeyStoreEncryptionStore(keyStore);
	}

	public static void main(String[] args) throws Exception {
		if (!args[0].equals("explode")) {
			throw new IllegalStateException();
		}

		char[] password = "notasecret".toCharArray();
		ProtectionParameter protParam = new KeyStore.PasswordProtection(password);

		KeyStore keyStore = KeyStoreUtils.load(new File(args[1]));
		File dest = new File(args[2]);
		dest.mkdirs();

		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();

			if (keyStore.isKeyEntry(alias)) {
				Entry entry = keyStore.getEntry(alias, protParam);
				PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) entry;

				{
					X509Certificate[] certificateChain = toX509(privateKeyEntry.getCertificateChain());
					String encoded = CertificateUtils.toPem(certificateChain);
					File out = new File(dest, alias + ".crt");
					Files.write(encoded, out, Charsets.UTF_8);
				}

				{
					PrivateKey key = privateKeyEntry.getPrivateKey();
					String encoded = PrivateKeys.toPem(key);
					File out = new File(dest, alias + ".key");
					Files.write(encoded, out, Charsets.UTF_8);
				}
			}

			if (keyStore.isCertificateEntry(alias)) {
				Entry entry = keyStore.getEntry(alias, null);
				TrustedCertificateEntry trustedCertificateEntry = (TrustedCertificateEntry) entry;

				X509Certificate cert = (X509Certificate) trustedCertificateEntry.getTrustedCertificate();
				String encoded = CertificateUtils.toPem(cert);
				File out = new File(dest, alias + ".crt");
				Files.write(encoded, out, Charsets.UTF_8);
			}
		}
	}

}
