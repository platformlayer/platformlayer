package org.platformlayer.ops.ldap;

import org.platformlayer.crypto.CryptoUtils;

public class LdapPasswords {
	public static String getLdapPasswordEncoded(String clearText) {
		if (clearText == null) {
			return null;
		}

		int saltBytes = 8;
		return LdapCrypto.encodeOffline(CryptoUtils.toBytesUtf8(clearText), saltBytes);
	}
}
