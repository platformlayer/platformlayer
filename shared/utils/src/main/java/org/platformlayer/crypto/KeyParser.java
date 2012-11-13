package org.platformlayer.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.openstack.utils.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				// TODO: Check if looks like base64??
				byte[] fromBase64 = CryptoUtils.fromBase64(s);

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
			byte[] encoded = CryptoUtils.fromBase64(data);
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
