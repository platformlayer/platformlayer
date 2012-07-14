package org.platformlayer.auth;

import javax.crypto.SecretKey;

public interface ProjectInfo {
	boolean isLocked();

	SecretKey getProjectSecret();

	int getId();

	String getName();
 }
