package org.platformlayer.service.jenkins.ops;

import org.platformlayer.crypto.CryptoUtils;

import com.fathomdb.utils.Base64;

/**
 * Supports password obfuscation in the way that Hudson does it
 */
public class HudsonScrambler {
	public static String scramble(String secret) {
		if (secret == null) {
			return null;
		}
		return Base64.encode(CryptoUtils.toBytesUtf8(secret));
	}

	public static String descramble(String scrambled) {
		if (scrambled == null) {
			return null;
		}
		return CryptoUtils.toStringUtf8(Base64.decode(scrambled));
	}
}
