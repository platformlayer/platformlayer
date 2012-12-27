package org.platformlayer.auth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

import org.platformlayer.crypto.CryptoUtils;

import com.fathomdb.Utf8;
import com.google.common.collect.Maps;

public class AuthenticationSignature {

	public static byte[] calculateSignature(Mac mac, String timestamp, String method, String requestPath) {
		// TODO: Add more parameters to strengthen this?
		TreeMap<String, String> signValues = Maps.newTreeMap();
		signValues.put("method", method);
		signValues.put("path", requestPath);
		signValues.put("t", timestamp);

		byte[] signData;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (Map.Entry<String, String> entry : signValues.entrySet()) {
				baos.write(Utf8.getBytes(entry.getKey()));
				baos.write(0);
				baos.write(Utf8.getBytes(entry.getValue()));
				baos.write(0);
			}
			signData = baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error signing message", e);
		}

		byte[] signature;

		synchronized (mac) {
			signature = CryptoUtils.computeMac(mac, signData);
		}
		return signature;
	}

	public static Mac buildMac(SecretKey secret) {
		// TODO: We use the (AES?) project secret to sign the request.
		// This should be OK, but we should check with a crypto guru here
		return CryptoUtils.buildHmacSha1(secret);
	}
}
