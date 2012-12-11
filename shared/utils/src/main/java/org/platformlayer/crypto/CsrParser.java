package org.platformlayer.crypto;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class CsrParser {
	private static final Logger log = LoggerFactory.getLogger(CsrParser.class);

	public CsrParser() {
	}

	// static final String BEGIN_PEM = "-----BEGIN CERTIFICATE REQUEST-----";
	// static final String END_PEM = "-----END CERTIFICATE REQUEST-----";

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

	public PKCS10CertificationRequest parse(String s) {
		PKCS10CertificationRequest key = null;

		if (key == null) {
			try {
				key = parsePemFormat(s);
			} catch (Exception e) {
				log.debug("Cannot debug as PEM", e);
			}
			// if (s.contains(BEGIN_PEM)) {
			// String payload = s.substring(s.indexOf(BEGIN_PEM) + BEGIN_PEM.length());
			//
			// if (payload.contains(END_PEM)) {
			// payload = payload.substring(0, payload.indexOf(END_PEM) + END_PEM.length());
			//
			// try {
			// key = parsePemFormat(payload);
			// } catch (Exception e) {
			// log.debug("Cannot debug as PEM", e);
			// }
			// }
			// }
		}

		// if (key == null) {
		// try {
		// // TODO: Check if looks like base64??
		// byte[] fromBase64 = CryptoUtils.fromBase64(s);
		//
		// key = parse(fromBase64);
		// } catch (Exception e) {
		// log.debug("Cannot decode as base64", e);
		// }
		// }

		return key;
	}

	private String tryDecodeAsString(byte[] data) {
		try {
			// We do this so we get strict input processing
			CharsetDecoder decoder = Charsets.UTF_8.newDecoder();
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

	private PKCS10CertificationRequest parsePemFormat(String data) throws IOException {
		PemReader reader = new PemReader(new StringReader(data));
		PemObject pemObject = reader.readPemObject();
		reader.close();

		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(pemObject.getContent());
		return csr;
	}
}
