package org.platformlayer.auth;

import java.security.PrivateKey;

import javax.crypto.SecretKey;

public interface OpsUser {

	int getId();

	SecretKey getUserSecret();

	PrivateKey getPrivateKey();

}
