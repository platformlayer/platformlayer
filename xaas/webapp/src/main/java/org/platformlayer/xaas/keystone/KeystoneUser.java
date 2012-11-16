package org.platformlayer.xaas.keystone;

import java.security.PrivateKey;

import org.apache.log4j.Logger;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.auth.system.PlatformlayerUserAuthentication;

public class KeystoneUser implements OpsUser {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KeystoneUser.class);

	private final PlatformlayerUserAuthentication auth;

	public KeystoneUser(PlatformlayerUserAuthentication auth) {
		this.auth = auth;
	}

	public PlatformlayerUserAuthentication getAuth() {
		return auth;
	}

	@Override
	public int getId() {
		return Integer.parseInt(auth.getUserKey());
	}

	// @Override
	// public SecretKey getUserSecret() {
	// byte[] userSecret = auth.getUserSecret();
	// if (userSecret == null) {
	// return null;
	// }
	// return AesUtils.deserializeKey(userSecret);
	// }

	@Override
	public PrivateKey getPrivateKey() {
		throw new UnsupportedOperationException();
	}
}
