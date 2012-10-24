package org.platformlayer.crypto;

import java.util.Arrays;

import javax.crypto.interfaces.PBEKey;

import com.fathomdb.crypto.KeyDerivationFunctions;

public class PasswordHash {
	static final int SALT_LENGTH = 16;
	static final int KEY_LENGTH = 128;

	public static byte[] doPasswordHash(String password) {
		byte[] salt = CryptoUtils.generateSecureRandom(SALT_LENGTH);
		int iterationCount = 1000;
		PBEKey key = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, KEY_LENGTH);

		return CryptoUtils.concat(salt, key.getEncoded());
	}

	public static boolean checkPasswordHash(byte[] hashed, String password) {
		if (hashed.length < SALT_LENGTH) {
			return false;
		}

		byte[] salt = Arrays.copyOfRange(hashed, 0, SALT_LENGTH);

		int iterationCount = 1000;
		PBEKey key = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, KEY_LENGTH);

		byte[] data = key.getEncoded();

		if ((data.length + SALT_LENGTH) != hashed.length) {
			return false;
		}

		// Try to prevent timing attacks
		int score = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] != hashed[SALT_LENGTH + i]) {
				score--;
			} else {
				score++;
			}
		}

		return score == data.length;
	}

}
