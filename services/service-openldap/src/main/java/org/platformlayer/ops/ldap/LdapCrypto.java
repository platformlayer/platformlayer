package org.platformlayer.ops.ldap;

import java.security.MessageDigest;
import java.security.SecureRandom;

import org.platformlayer.crypto.CryptoUtils;

import com.fathomdb.utils.Base64;

public class LdapCrypto {
	static final SecureRandom secureRandom = new SecureRandom();

	public static String encodeOffline(byte[] passwordBytes, int saltByteCount) {
		byte[] saltBytes = new byte[saltByteCount];

		synchronized (secureRandom) {
			secureRandom.nextBytes(saltBytes);
		}

		MessageDigest messageDigest = CryptoUtils.getSha1();
		messageDigest.update(passwordBytes);
		messageDigest.update(saltBytes);
		byte[] digest = messageDigest.digest();

		byte[] digestAndSalt = new byte[digest.length + saltBytes.length];
		System.arraycopy(digest, 0, digestAndSalt, 0, digest.length);
		System.arraycopy(saltBytes, 0, digestAndSalt, digest.length, saltBytes.length);

		return "{SSHA}" + Base64.encode(digestAndSalt);
	}
}
