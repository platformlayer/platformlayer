package org.platformlayer.xaas.keystone;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.openstack.keystone.service.KeystoneAuthentication;
import org.platformlayer.auth.OpsUser;
import org.platformlayer.crypto.AesUtils;

public class KeystoneUser implements OpsUser {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(KeystoneUser.class);

	private final KeystoneAuthentication auth;

	public KeystoneUser(KeystoneAuthentication auth) {
		this.auth = auth;
	}

	public KeystoneAuthentication getAuth() {
		return auth;
	}

	@Override
	public int getId() {
		return Integer.parseInt(auth.getUserKey());
	}

	@Override
	public SecretKey getUserSecret() {
		byte[] userSecret = auth.getUserSecret();
		if (userSecret == null) {
			return null;
		}
		return AesUtils.deserializeKey(userSecret);
	}

	@Override
	public PrivateKey getPrivateKey() {
		throw new UnsupportedOperationException();
	}
}
