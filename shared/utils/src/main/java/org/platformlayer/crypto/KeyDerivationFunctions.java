package org.platformlayer.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;

public class KeyDerivationFunctions {

    public static PBEKey doPbkdf2(byte[] salt, String password, int keyLength) {
        int iterationCount = 1000;

        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to get PBKDF2 provider", e);
        }
        PBEKey key;
        try {
            key = (PBEKey) factory.generateSecret(pbeKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Error generating secret", e);
        }

        return key;
    }
}
