package org.platformlayer.crypto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.log4j.Logger;
import org.openstack.utils.Utf8;

public class KeyParser {
	private static final Logger log = Logger.getLogger(KeyParser.class);

	final byte[] data;

	public KeyParser(byte[] data) {
		super();
		this.data = data;
	}

	static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
	static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";

	public Object tryParse() {
		Object key = null;

		String s = tryDecodeAsString();
		if (s != null) {
			if (s.contains(BEGIN_PRIVATE_KEY)) {
				String data = s.substring(s.indexOf(BEGIN_PRIVATE_KEY) + BEGIN_PRIVATE_KEY.length());

				if (!data.contains(END_PRIVATE_KEY)) {
					// Corrupt
					return null;
				}

				data = data.substring(0, data.indexOf(END_PRIVATE_KEY));

				key = tryParsePrivateKey(data);
				if (key != null) {
					return key;
				}
			}
		}

		log.warn("Unable to decode key: " + s);

		return null;
	}

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

	private PrivateKey tryParsePrivateKey(String data) {
		try {
			byte[] encoded = CryptoUtils.fromBase64(data);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privKey = kf.generatePrivate(keySpec);

			return privKey;
		} catch (Exception e) {
			log.debug("Unable to parse pem data", e);
			return null;
		}
	}

	public Object parse() {
		Object key = tryParse();
		if (key != null) {
			return key;
		}
		throw new IllegalStateException("Cannot decode key data");
	}

	private String tryDecodeAsString() {
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
}
