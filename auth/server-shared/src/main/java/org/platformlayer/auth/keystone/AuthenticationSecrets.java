package org.platformlayer.auth.keystone;

import com.fathomdb.crypto.CryptoKey;
import com.google.inject.ImplementedBy;

@ImplementedBy(SimpleAuthenticationSecrets.class)
public interface AuthenticationSecrets {

	CryptoKey decryptSecretFromToken(byte[] tokenSecret);

	byte[] buildToken(CryptoKey userSecret);

}
