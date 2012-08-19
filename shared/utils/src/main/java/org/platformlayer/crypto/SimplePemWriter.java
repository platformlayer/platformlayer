package org.platformlayer.crypto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

public class SimplePemWriter extends BufferedWriter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimplePemWriter.class);

	public SimplePemWriter(Writer writer) {
		super(writer);
	}

	public void writeObject(X509Certificate cert) throws IOException {
		byte[] encoded;
		try {
			encoded = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new IllegalArgumentException("Error encoding certificate", e);
		}
		writeBegin(PemConstants.TYPE_CERTIFICATE);
		writeEncoded(encoded);
		writeEnd(PemConstants.TYPE_CERTIFICATE);
	}

	public void writeObject(KeyPair cert) throws IOException {
		byte[] encoded;
		encoded = cert.getPrivate().getEncoded();

		writeBegin(PemConstants.TYPE_RSA_PRIVATE_KEY);
		writeEncoded(encoded);
		writeEnd(PemConstants.TYPE_RSA_PRIVATE_KEY);
	}

	private void writeBegin(String type) throws IOException {
		write(PemConstants.BEGIN_PREFIX);
		write(type);
		write(PemConstants.BEGIN_SUFFIX);
		newLine();
	}

	private void writeEnd(String type) throws IOException {
		write(PemConstants.END_PREFIX);
		write(type);
		write(PemConstants.END_SUFFIX);
		newLine();
	}

	private void writeEncoded(byte[] data) throws IOException {
		String base64 = CryptoUtils.toBase64(data);

		int off = 0;
		while (off < base64.length()) {
			int n = Math.min(PemConstants.LINE_LENGTH, base64.length() - off);
			write(base64, off, n);
			newLine();
			off += n;
		}
	}

}
