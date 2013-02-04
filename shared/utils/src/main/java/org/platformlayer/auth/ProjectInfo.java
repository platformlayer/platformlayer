package org.platformlayer.auth;

import com.fathomdb.crypto.CryptoKey;

public interface ProjectInfo {
	boolean isLocked();

	CryptoKey getProjectSecret();

	String getName();

	int getId();
}
