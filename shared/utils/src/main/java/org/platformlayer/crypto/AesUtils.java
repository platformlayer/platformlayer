package org.platformlayer.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.SecretKeySpec;

public class AesUtils {
    private static final String ALGORITHM = "AES";
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
        return CryptoUtils.getCipher(ALGORITHM);
    }

    public static byte[] decrypt(SecretKey key, byte[] cipherText) {
        return CryptoUtils.decrypt(getCipher(), key, cipherText);
    }

    public static byte[] serialize(SecretKey key) {
        return key.getEncoded();
    }

    public static SecretKey deserializeKey(byte[] keyData) {
        SecretKeySpec key = new SecretKeySpec(keyData, ALGORITHM);
        return key;
    }

    public static SecretKey deriveKey(byte[] salt, String password) {
        PBEKey pbeKey = KeyDerivationFunctions.doPbkdf2(salt, password, DEFAULT_KEYSIZE);
        SecretKey secretKey = new SecretKeySpec(pbeKey.getEncoded(), ALGORITHM);
        return secretKey;
    }
}
