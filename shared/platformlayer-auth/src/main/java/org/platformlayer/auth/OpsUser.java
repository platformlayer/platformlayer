package org.platformlayer.auth;

import java.security.PrivateKey;

public interface OpsUser {
	int getId();

	// SecretKey getUserSecret();

	PrivateKey getPrivateKey();
}
