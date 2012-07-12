package org.platformlayer.crypto;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;

public class AcceptAllHostnameVerifier implements HostnameVerifier {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AcceptAllHostnameVerifier.class);

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}
