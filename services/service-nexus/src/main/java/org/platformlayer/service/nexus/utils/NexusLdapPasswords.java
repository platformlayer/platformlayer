package org.platformlayer.service.nexus.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.platformlayer.crypto.CryptoUtils;

/**
 * This code (should) reproduce the Nexus LDAP password encryption / decryption
 * 
 */
public class NexusLdapPasswords {
	private final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();

	public boolean addEscapeCharacters = true;
	public int iterationCount = 23;
	public int saltSize = 8;

	// This is the hard-coded passphrase from the nexus code
	public String passphrase = "CMMDwoV";

	static final String KEY_ALGORITHM = "PBEWithSHAAnd128BitRC4";

	public String encrypt(String plaintext) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
		String ciphertext = encrypt(plaintext, passphrase);
		if (addEscapeCharacters) {
			ciphertext = "{" + ciphertext + "}";
		}
		return ciphertext;
	}

	public String decrypt(String ciphertext) throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IOException {
		return decrypt(ciphertext, passphrase);
	}

	private byte[] generateSalt(int saltSize) throws NoSuchAlgorithmException, NoSuchProviderException {
		SecureRandom sr = new SecureRandom();
		// Note that the nexus code seeds the SecureRandom:
		// this seems to be both unnecessary and incorrect
		return sr.generateSeed(saltSize);
	}

	private Cipher buildCipher(String passphrase, byte[] salt, int mode) throws InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		KeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
		SecretKey key = SecretKeyFactory.getInstance(KEY_ALGORITHM, bouncyCastleProvider).generateSecret(keySpec);

		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, bouncyCastleProvider);
		PBEParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

		cipher.init(mode, key, paramSpec);
		return cipher;
	}

	public String encrypt(String plaintextString, String passphrase) throws NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {

		byte[] salt = generateSalt(saltSize);
		Cipher cipher = buildCipher(passphrase, salt, Cipher.ENCRYPT_MODE);

		byte[] plaintext = CryptoUtils.toBytesUtf8(plaintextString);
		byte[] ciphertext = cipher.doFinal(plaintext);

		// We record the salt length, then the salt, then the ciphertext
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(salt.length);
		baos.write(salt);
		baos.write(ciphertext);

		return CryptoUtils.toBase64(baos.toByteArray());
	}

	public String decrypt(String ciphertextString, String passphrase) throws IllegalBlockSizeException,
			BadPaddingException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, IOException {
		ciphertextString = removeDecoration(ciphertextString);

		byte[] ciphertextData = CryptoUtils.fromBase64(ciphertextString);
		ByteArrayInputStream bais = new ByteArrayInputStream(ciphertextData);

		int saltLen = bais.read();
		if (saltLen < 0) {
			throw new IllegalArgumentException();
		}

		byte[] salt = new byte[saltLen];
		if (saltLen != bais.read(salt)) {
			throw new IllegalArgumentException();
		}

		int ciphertextLength = bais.available();
		if (ciphertextLength <= 0) {
			throw new IllegalArgumentException();
		}

		byte[] ciphertext = new byte[ciphertextLength];
		if (ciphertextLength != bais.read(ciphertext)) {
			throw new IllegalArgumentException();
		}

		Cipher cipher = buildCipher(passphrase, salt, Cipher.DECRYPT_MODE);
		byte[] plaintext = cipher.doFinal(ciphertext);

		return new String(plaintext, "UTF8");
	}

	private String removeDecoration(String s) {
		if (s.contains("{") && s.contains("}")) {
			return s.substring(s.indexOf("{") + 1, s.indexOf("}"));
		}
		return s;
	}

}
