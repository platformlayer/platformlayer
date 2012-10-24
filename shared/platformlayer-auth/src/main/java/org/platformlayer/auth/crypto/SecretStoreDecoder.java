package org.platformlayer.auth.crypto;

import java.security.PrivateKey;

import org.platformlayer.crypto.RsaUtils;

import com.fathomdb.crypto.CryptoKey;
import com.fathomdb.crypto.FathomdbCrypto;

public class SecretStoreDecoder extends SecretStoreVisitor {
	CryptoKey secretKey;

	public Object result;

	protected CryptoKey toSecretKey(byte[] data) {
		return FathomdbCrypto.deserializeKey(data);
	}

	protected CryptoKey decryptAsymetricKey(PrivateKey privateKey, byte[] encrypted) {
		return toSecretKey(RsaUtils.decrypt(privateKey, encrypted));
	}

	protected CryptoKey decryptSymetricKey(CryptoKey userKey, byte[] encrypted) {
		return toSecretKey(FathomdbCrypto.decrypt(userKey, encrypted));
	}

	public CryptoKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(CryptoKey key) {
		if (this.secretKey != null) {
			if (!this.secretKey.equals(key)) {
				throw new IllegalStateException();
			}
		}

		this.secretKey = key;
	}

}
