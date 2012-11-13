package org.platformlayer.crypto;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BouncyCastleLoader {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BouncyCastleLoader.class);

	static BouncyCastleProvider provider;

	static {
		ensureLoaded();
	}

	public static String getName() {
		ensureLoaded();
		return BouncyCastleProvider.PROVIDER_NAME;
	}

	private static void ensureLoaded() {
		synchronized (BouncyCastleLoader.class) {
			if (provider == null) {
				BouncyCastleProvider bc = new BouncyCastleProvider();
				Security.addProvider(bc);
				provider = bc;
			}
		}
	}
}
