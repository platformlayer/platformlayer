package org.platformlayer.http.apache;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheHostnameVerifierAdapter implements X509HostnameVerifier {
	private static final Logger log = LoggerFactory.getLogger(ApacheHostnameVerifierAdapter.class);

	final HostnameVerifier hostnameVerifier;

	public ApacheHostnameVerifierAdapter(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return hostnameVerifier.verify(hostname, session);
	}

	@Override
	public void verify(String host, SSLSocket socket) throws IOException {
		if (socket instanceof sun.security.ssl.SSLSocketImpl) {
			log.debug("Setting SNI host=" + host);
			((sun.security.ssl.SSLSocketImpl) socket).setHost(host);
		} else {
			log.warn("Socket of unexpected type; can't set SNI: " + socket.getClass());
		}

		SSLSession session = socket.getSession();
		delegateVerification(host, session);
	}

	private void delegateVerification(String host, SSLSession session) throws IOException {
		if (session == null) {
			throw new IllegalStateException();
		}

		if (!hostnameVerifier.verify(host, session)) {
			throw new IOException("SSL hostname did not match");
		}
	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
		throw new UnsupportedOperationException();
	}
}
