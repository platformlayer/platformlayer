package org.platformlayer.web;

import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class HttpUtils {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(HttpUtils.class);

	public static X509Certificate[] getCertificateChain(HttpServletRequest request) {
		X509Certificate[] certChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		if (certChain == null || certChain.length == 0) {
			return null;
		}
		return certChain;
	}
}
