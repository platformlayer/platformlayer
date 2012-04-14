package org.platformlayer.service.jenkins.ops;

import org.platformlayer.crypto.CryptoUtils;

/**
 * Supports password obfuscation in the way that Hudson does it
 */
public class HudsonScrambler {
	public static String scramble(String secret) {
		if (secret == null) {
			return null;
		}
		return CryptoUtils.toBase64(CryptoUtils.toBytesUtf8(secret));
	}

	public static String descramble(String scrambled) {
		if (scrambled == null) {
			return null;
		}
		return CryptoUtils.toStringUtf8(CryptoUtils.fromBase64(scrambled));
	}
}
