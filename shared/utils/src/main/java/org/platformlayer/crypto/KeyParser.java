package org.platformlayer.crypto;

import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Utf8;
import com.fathomdb.utils.Base64;

public class KeyParser {
	private static final Logger log = LoggerFactory.getLogger(KeyParser.class);

	public KeyParser() {
	}

	static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
	static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";

	// private KeyPair tryParsePrivateKey(String data) {
	// PEMReader pemReader = new PEMReader(new StringReader(data), new PasswordFinder() {
	//
	// @Override
	// public char[] getPassword() {
	// return "notasecret".toCharArray();
	// }
	// });
	// try {
	// return (KeyPair) pemReader.readObject();
	// } catch (IOException e) {
	// log.debug("Unable to parse pem data", e);
	// return null;
	// } finally {
	// Io.safeClose(pemReader);
	// }
	// }

	public Object parse(byte[] data) {
		Object key = null;

		if (key == null) {
			String s = tryDecodeAsString(data);
			if (s != null) {
				key = parse(s);
			}
		}

		return null;
	}

	public Object parse(String s) {
		Object key = null;

		if (key == null) {
			if (s.contains(BEGIN_PRIVATE_KEY)) {
				String payload = s.substring(s.indexOf(BEGIN_PRIVATE_KEY) + BEGIN_PRIVATE_KEY.length());

				if (payload.contains(END_PRIVATE_KEY)) {
					payload = payload.substring(0, payload.indexOf(END_PRIVATE_KEY));

					key = tryParsePemFormat(payload);
				}
			}
		}

		if (key == null) {
			try {
				PemReader reader = new PemReader(new StringReader(s));
				PemObject pemObject = reader.readPemObject();
				reader.close();

				PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
				KeyFactory kf = KeyFactory.getInstance("RSA");
				PrivateKey privateKey = kf.generatePrivate(keySpec);
				if (privateKey instanceof RSAPrivateCrtKey) {
					RSAPrivateCrtKey rsaPrivateCrtKey = (RSAPrivateCrtKey) privateKey;
					RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(
							rsaPrivateCrtKey.getModulus(), rsaPrivateCrtKey.getPublicExponent());
					PublicKey publicKey = kf.generatePublic(publicKeySpec);
					key = new KeyPair(publicKey, privateKey);
				} else {
					key = privateKey;
				}
			} catch (Exception e) {
				log.debug("Error reading pem data", e);
				return null;
			}
		}

		if (key == null) {
			try {
				// TODO: Check if looks like base64??
				byte[] fromBase64 = Base64.decode(s);

				key = parse(fromBase64);
			} catch (Exception e) {
				log.debug("Cannot decode as base64", e);
			}
		}

		return key;
	}

	private String tryDecodeAsString(byte[] data) {
		try {
			CharsetDecoder decoder = Utf8.CHARSET.newDecoder();
			decoder.onMalformedInput(CodingErrorAction.REPORT);
			decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

			ByteBuffer byteBuffer = ByteBuffer.wrap(data);
			CharBuffer charBuffer = decoder.decode(byteBuffer);
			return charBuffer.toString();
		} catch (Exception e) {
			log.debug("Cannot decode as string", e);
			return null;
		}
	}

	private PrivateKey tryParsePemFormat(String data) {
		try {
			byte[] encoded = Base64.decode(data);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privKey = kf.generatePrivate(keySpec);

			return privKey;
		} catch (Exception e) {
			log.debug("Error parsing pem data", e);
			return null;
		}
	}
}
