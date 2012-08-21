package org.platformlayer.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.KeyStoreUtils;
import org.platformlayer.IoUtils;

import com.google.common.collect.Lists;

public class CertificateUtils {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CertificateUtils.class);

	public static byte[] serialize(Certificate certificate) {
		try {
			return certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new IllegalArgumentException("Error encoding certificate", e);
		}
	}

	public static byte[] serialize(Certificate[] certificateChain) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (Certificate certificate : certificateChain) {
			try {
				baos.write(serialize(certificate));
			} catch (IOException e) {
				throw new IllegalArgumentException("Error encoding certificates", e);
			}
		}

		return baos.toByteArray();
	}

	public static X509Certificate[] deserialize(byte[] data) {
		CertificateReader reader = new CertificateReader();
		return reader.parse(data);
	}

	public static String toPem(X509Certificate... certs) {
		return toPem(Lists.newArrayList(certs));
	}

	public static String toPem(Iterable<X509Certificate> certs) {
		try {
			StringWriter stringWriter = new StringWriter();

			PEMWriter writer = new PEMWriter(stringWriter, BouncyCastleLoader.getName());
			for (X509Certificate cert : certs) {
				writer.writeObject(cert);
			}
			writer.close();

			String s = stringWriter.toString();
			return s;
		} catch (IOException e) {
			throw new IllegalArgumentException("Error serializing certificates", e);
		}
	}

	public static List<X509Certificate> fromPem(String cert) {
		List<X509Certificate> certificates = Lists.newArrayList();

		PEMReader reader = null;
		try {
			reader = new PEMReader(new StringReader(cert), null, BouncyCastleLoader.getName());
			while (true) {
				Object o = reader.readObject();
				if (o == null) {
					return certificates;
				}

				certificates.add((X509Certificate) o);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error parsing certificate", e);
		} finally {
			IoUtils.safeClose(reader);
		}
	}

	public static List<X509Certificate> fromPem(File path) throws IOException {
		return fromPem(IoUtils.readAll(path));
	}

	public static CertificateAndKey createSelfSigned(X500Principal principal, int keySize) {
		try {
			String keyAlgorithmName = "RSA";
			String signatureAlgName = "SHA1WithRSA";

			String keyPassword = KeyStoreUtils.DEFAULT_KEYSTORE_SECRET;

			int validityDays = 365 * 10;

			String alias = "self";

			sun.security.x509.X500Name x500Name = new sun.security.x509.X500Name(
					principal.getName(X500Principal.RFC2253));

			KeyStore keyStore = KeyStoreUtils.createEmpty(KeyStoreUtils.DEFAULT_KEYSTORE_SECRET);
			KeyStoreUtils.createSelfSigned(keyStore, alias, keyPassword, x500Name, validityDays, keyAlgorithmName,
					keySize, signatureAlgName);

			return KeyStoreUtils.getCertificateAndKey(keyStore, alias, keyPassword);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("Error creating self-signed certificate", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error creating self-signed certificate", e);
		}
	}

}
