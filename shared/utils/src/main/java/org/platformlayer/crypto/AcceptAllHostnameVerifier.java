package org.platformlayer.crypto;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class AcceptAllHostnameVerifier implements HostnameVerifier {
	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}
