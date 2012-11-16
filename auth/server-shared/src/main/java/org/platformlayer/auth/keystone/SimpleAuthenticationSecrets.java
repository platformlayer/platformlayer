package org.platformlayer.auth.keystone;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.crypto.CryptoUtils;

import com.fathomdb.Configuration;
import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;

@Singleton
public class SimpleAuthenticationSecrets implements AuthenticationSecrets {

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

		byte[] ciphertext = Arrays.copyOfRange(tokenSecret, 1, tokenSecret.length);
		byte[] plaintext = secret.decrypt(ciphertext);
		return FathomdbCrypto.deserializeKey(plaintext);
	}

	@Override
	public byte[] buildToken(CryptoKey userSecret) {
		byte tokenId = currentTokenId;
		CryptoKey secret = secrets.get(tokenId);
		if (secret == null) {
			throw new IllegalStateException();
		}

		byte[] plaintext = FathomdbCrypto.serialize(userSecret);
		byte[] ciphertext = secret.encrypt(plaintext);

		byte[] header = new byte[1];
		header[0] = tokenId;
		return Bytes.concat(header, ciphertext);
	}
}
