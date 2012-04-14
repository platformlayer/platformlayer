package org.platformlayer.auth.crypto;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.platformlayer.crypto.AesUtils;
import org.platformlayer.crypto.RsaUtils;

public class SecretStoreDecoder extends SecretStoreVisitor {
	SecretKey secretKey;

	protected SecretKey toSecretKey(byte[] data) {
		return AesUtils.deserializeKey(data);
	}

	protected SecretKey decryptAsymetricKey(PrivateKey privateKey, byte[] encrypted) {
		return toSecretKey(RsaUtils.decrypt(privateKey, encrypted));
	}

	protected SecretKey decryptSymetricKey(SecretKey userKey, byte[] encrypted) {
		return toSecretKey(AesUtils.decrypt(userKey, encrypted));
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey key) {
		if (this.secretKey != null) {
			if (!this.secretKey.equals(key)) {
				throw new IllegalStateException();
			}
		}

		this.secretKey = key;
	}

}
