package org.platformlayer.auth.keystone;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.platformlayer.config.Configuration;
import org.platformlayer.crypto.CryptoUtils;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;

@Singleton
public class SimpleAuthenticationSecrets implements AuthenticationSecrets {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SimpleAuthenticationSecrets.class);

	final Map<Byte, CryptoKey> secrets = Maps.newHashMap();

	byte currentTokenId;

	@Inject
	public SimpleAuthenticationSecrets(Configuration configuration) {
		this.currentTokenId = 1;

		String secret = configuration.find("sharedsecret");
		if (secret == null) {
			throw new IllegalStateException("sharedsecret is required");
		}

		// This isn't ideal, but it needs to be consistent
		byte[] salt = CryptoUtils.sha1(secret).toByteArray();

		CryptoKey authSecret = FathomdbCrypto.deriveKeyRaw(1000, salt, secret);
		secrets.put(this.currentTokenId, authSecret);
	}

	@Override
	public CryptoKey decryptSecretFromToken(byte[] tokenSecret) {
		if (tokenSecret.length <= 2) {
			return null;
		}

		byte tokenId = tokenSecret[0];
		CryptoKey secret = secrets.get(tokenId);
		if (secret == null) {
			return null;
		}

		int ivLength = (tokenSecret[1] & 0xff);
		byte[] iv = Arrays.copyOfRange(tokenSecret, 2, 2 + ivLength);
		byte[] tail = Arrays.copyOfRange(tokenSecret, 2 + ivLength, tokenSecret.length);

		byte[] plaintext = secret.decrypt(iv, tail);
		return FathomdbCrypto.deserializeKey(plaintext);
	}

	@Override
	public byte[] buildToken(CryptoKey userSecret) {
		byte tokenId = currentTokenId;
		CryptoKey secret = secrets.get(tokenId);
		if (secret == null) {
			throw new IllegalStateException();
		}

		byte ivLength = 16;
		byte[] iv = FathomdbCrypto.generateSecureRandom(ivLength);

		byte[] plaintext = FathomdbCrypto.serialize(userSecret);
		byte[] ciphertext = secret.encrypt(iv, plaintext);

		byte[] header = new byte[2];
		header[0] = tokenId;
		header[1] = ivLength;
		return Bytes.concat(header, iv, ciphertext);
	}
}
