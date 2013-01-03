package org.platformlayer.crypto;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class PublicKeyTrustManager implements X509TrustManager {
	private static final Logger log = LoggerFactory.getLogger(PublicKeyTrustManager.class);

	private final Set<String> trusted;

	private final boolean checkAll;

	public PublicKeyTrustManager(Iterable<String> keys, boolean checkAll) {
		this.checkAll = checkAll;
		this.trusted = Sets.newHashSet();
		Iterables.addAll(this.trusted, keys);
	}

	public PublicKeyTrustManager(Iterable<String> keys) {
		this(keys, false);
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (chain.length == 0) {
			throw new IllegalArgumentException("null or zero-length certificate chain");
		}

		for (X509Certificate cert : chain) {
			PublicKey certPublicKey = cert.getPublicKey();

			String sigString = OpenSshUtils.getSignatureString(certPublicKey);

			if (!trusted.contains(sigString)) {
				log.warn("Certificate is not in trusted list (" + sigString + ")");
				throw new CertificateException("Certificate is not in trusted list (" + sigString + ")");
			}

			if (!checkAll) {
				log.debug("First certificate matched key; checkAll is false: done!");
				break;
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "PublicKeyTrustManager [trusted=" + trusted + "]";
	}

}
