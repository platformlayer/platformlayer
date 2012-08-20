package org.platformlayer.crypto;

import java.security.Security;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BouncyCastleLoader {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BouncyCastleLoader.class);

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
