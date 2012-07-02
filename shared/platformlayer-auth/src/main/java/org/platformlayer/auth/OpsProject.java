package org.platformlayer.auth;

import javax.crypto.SecretKey;

public interface OpsProject {
	boolean isLocked();

	SecretKey getProjectSecret();

	int getId();

	String getName();
}
