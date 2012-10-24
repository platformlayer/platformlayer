package org.platformlayer.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

import com.fathomdb.crypto.KeyDerivationFunctions;

public class AesCbcUtils {
	private static final String ALGORITHM = "AES";
	private static final String CIPHER = "AES/CBC/PKCS5Padding";
	private static final int DEFAULT_KEYSIZE = 128;

	public static SecretKey generateKey() {
		return generateKey(DEFAULT_KEYSIZE);
	}

	public static SecretKey generateKey(int keysize) {
		return CryptoUtils.generateKey(ALGORITHM, keysize);
	}

	public static byte[] encrypt(SecretKey key, byte[] plaintext) {
		return CryptoUtils.encrypt(getCipher(), key, plaintext);
	}

	private static Cipher getCipher() {
		return CryptoUtils.getCipher(CIPHER);
	}

	public static byte[] decrypt(SecretKey key, byte[] cipherText) {
		return CryptoUtils.decrypt(getCipher(), key, cipherText);
	}

	public static Cipher buildDecryptor(SecretKey key) {
		Cipher cipher = getCipher();
		CryptoUtils.initDecrypt(cipher, key);
		return cipher;
	}

	public static Cipher buildEncryptor(SecretKey key) {
		Cipher cipher = getCipher();
		CryptoUtils.initEncrypt(cipher, key);
		return cipher;
	}

	public static byte[] serialize(SecretKey key) {
		return key.getEncoded();
	}

	public static SecretKey deserializeKey(byte[] keyData) {
		SecretKeySpec key = new SecretKeySpec(keyData, ALGORITHM);
		return key;
	}

	public static SecretKey deriveKey(byte[] salt, String password) {
		int iterationCount = 1000;
		PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(iterationCount, salt, password, DEFAULT_KEYSIZE);
		SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), ALGORITHM);
		return secretKey;
	}

	public static long computeEncryptedLength(long contentLength) {
		long length = 16 * ((contentLength + 15) / 16);
		return length;
	}
}
