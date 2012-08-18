package org.platformlayer.crypto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.platformlayer.IoUtils;
import org.platformlayer.ops.OpsException;

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

	public static X509Certificate[] deserialize(byte[] data) throws OpsException {
		CertificateReader reader = new CertificateReader();
		return reader.parse(data);
	}

	public static String toPem(X509Certificate... certs) {
		return toPem(Lists.newArrayList(certs));
	}

	public static String toPem(Iterable<X509Certificate> certs) {
		try {
			StringWriter stringWriter = new StringWriter();
			PEMWriter writer = new PEMWriter(stringWriter);
			for (X509Certificate cert : certs) {
				writer.writeObject(cert);
			}
			writer.close();

			// StringBuilder sb = new StringBuilder();
			// sb.append("-----BEGIN CERTIFICATE-----\n");
			// sb.append(CryptoUtils.toBase64(machineCertificate.getEncoded()));
			// sb.append("\n-----END CERTIFICATE-----\n");

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
			reader = new PEMReader(new StringReader(cert));
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
}
