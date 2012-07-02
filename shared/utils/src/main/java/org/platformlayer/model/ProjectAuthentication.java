package org.platformlayer.model;

import javax.crypto.SecretKey;

public interface ProjectAuthentication {
	String getName();

	String getId();

	SecretKey getSecret();
}
