package org.platformlayer.crypto;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;

public class SimplePemReader extends BufferedReader {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimplePemReader.class);

	public SimplePemReader(Reader reader) {
		super(reader);
	}

	public Object readObject() throws IOException {
		String line = readLine();

		while (line != null && !line.startsWith(PemConstants.BEGIN_PREFIX)) {
			line = readLine();
		}

		if (line != null) {
			line = line.substring(PemConstants.BEGIN_PREFIX.length());
			int index = line.indexOf('-');
			if (index != -1) {
				String type = line.substring(0, index);
				type = type.trim();

				return loadObject(type);
			}
		}

		return null;
	}

	private Object loadObject(String type) throws IOException {
		StringBuilder base64 = new StringBuilder();
		Map<String, String> headers = Maps.newHashMap();

		while (true) {
			String line = readLine();
			if (line == null) {
				throw new IOException("Did not find end line");
			}

			int colonIndex = line.indexOf(':');
			if (colonIndex != -1) {
				String key = line.substring(0, colonIndex);
				String value = line.substring(colonIndex + 1);

				key = key.trim();
				value = value.trim();

				// Should we use multimap? (We're ignoring them anyway now)
				headers.put(key, value);

				continue;
			}

			if (line.startsWith(PemConstants.END_PREFIX)) {
				break;
			}

			base64.append(line.trim());
		}

		return buildObject(type, headers, CryptoUtils.fromBase64(base64.toString()));
	}

	private Object buildObject(String type, Map<String, String> headers, byte[] data) {
		if (type.equals(PemConstants.TYPE_CERTIFICATE)) {
			try {
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
				// TODO: Multiple certificates??
				ByteArrayInputStream is = new ByteArrayInputStream(data);
				return certificateFactory.generateCertificate(is);
			} catch (CertificateException e) {
				throw new IllegalArgumentException("Error parsing certificate", e);
			}
		} else if (type.equals(PemConstants.TYPE_RSA_PRIVATE_KEY)) {
			try {
				PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PublicKey publicKey = kf.generatePublic(keySpec);
				PrivateKey privateKey = kf.generatePrivate(keySpec);

				return new KeyPair(publicKey, privateKey);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalArgumentException("Error parsing private key", e);
			} catch (InvalidKeySpecException e) {
				throw new IllegalArgumentException("Error parsing private key", e);
			}
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

}
