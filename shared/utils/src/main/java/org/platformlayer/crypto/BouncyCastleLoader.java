package org.platformlayer.crypto;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BouncyCastleLoader {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BouncyCastleLoader.class);

	static final BouncyCastleProvider provider;

	static {
		provider = new BouncyCastleProvider();
	}

	public static String getName() {
		return BouncyCastleProvider.PROVIDER_NAME;
	}
}
