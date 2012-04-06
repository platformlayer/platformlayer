package org.platformlayer.crypto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.platformlayer.ExceptionUtils;
import org.platformlayer.IoUtils;

public class RsaUtils {
    private static final String ALGORITHM = "RSA";
    // private static final String PROVIDER = "SunJSSE";
    private static final int DEFAULT_KEYSIZE = 2048;

    public static KeyPair generateRsaKeyPair() {
        return generateRsaKeyPair(DEFAULT_KEYSIZE);
    }

    public static KeyPair generateRsaKeyPair(int keysize) {
        return CryptoUtils.generateKeyPair(ALGORITHM, keysize);
    }

    public static byte[] serialize(PrivateKey key) {
        // TODO: This is a very large representation (it optimizes for CPU later by storing extra values)
        // Is this the right trade off?
        // See http://stackoverflow.com/questions/2921508/trying-to-understand-java-rsa-key-size

        return key.getEncoded();
    }

    public static byte[] serialize(PublicKey key) {
        return key.getEncoded();
    }

    public static PublicKey deserializePublicKey(byte[] keyData) {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyData);

        KeyFactory keyFactory = getKeyFactory();

        PublicKey pubKey;
        try {
            pubKey = keyFactory.generatePublic(pubKeySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Error deserializing public key", e);
        }
        return pubKey;
    }

    public static PrivateKey deserializePrivateKey(byte[] keyData) {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyData);

        KeyFactory keyFactory = getKeyFactory();

        PrivateKey privateKey;
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Error deserializing private key", e);
        }
        return privateKey;
    }

    private static KeyFactory getKeyFactory() {
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error loading RSA provider", e);
        }
        return keyFactory;
    }

    //
    // public static void writeToFile(Key key, File file) throws IOException {
    // byte[] data = serialize(key);
    // IoUtils.writeAllBinary(file, data);
    // }

    public static PublicKey loadPublicKey(File file) throws IOException {
        return loadPublicKey(IoUtils.readAllBinary(file));
    }

    public static PrivateKey loadPrivateKey(File file) throws IOException {
        return loadPrivateKey(IoUtils.readAllBinary(file));
    }

    // public static PrivateKey loadPrivateKeyFromResource(String resourceName) throws IOException {
    // InputStream resourceAsStream = null;
    // try {
    // resourceAsStream = RsaUtils.class.getClassLoader().getResourceAsStream(resourceName);
    // if (resourceAsStream == null)
    // throw new IllegalArgumentException("Cannot find resource: " + resourceName);
    //
    // return loadPrivateKey(IoUtils.readAllBinary(resourceAsStream));
    // } finally {
    // IoUtils.safeClose(resourceAsStream);
    // }
    // }

    public static PublicKey loadPublicKeyFromResource(String resourceName) throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = RsaUtils.class.getClassLoader().getResourceAsStream(resourceName);
            if (resourceAsStream == null)
                throw new IllegalArgumentException("Cannot find resource: " + resourceName);

            return loadPublicKey(IoUtils.readAllBinary(resourceAsStream));
        } finally {
            IoUtils.safeClose(resourceAsStream);
        }
    }

    public static PublicKey loadPublicKey(byte[] keyData) throws IOException {
        try {
            return deserializePublicKey(keyData);
        } catch (Exception e) {
            ExceptionUtils.handleInterrupted(e);
            throw new IOException("Error while loading key", e);
        }
    }

    public static PrivateKey loadPrivateKey(byte[] keyData) throws IOException {
        try {
            PrivateKey privateKey = deserializePrivateKey(keyData);
            return privateKey;
        } catch (Exception e) {
            ExceptionUtils.handleInterrupted(e);
            throw new IOException("Error while loading key", e);
        }
    }

    public static byte[] encrypt(Key key, byte[] plaintext) {
        return CryptoUtils.encrypt(getCipher(), key, plaintext);
    }

    private static Cipher getCipher() {
        return CryptoUtils.getCipher(ALGORITHM);
    }

    public static byte[] decrypt(Key key, byte[] cipherText) {
        return CryptoUtils.decrypt(getCipher(), key, cipherText);
    }

}