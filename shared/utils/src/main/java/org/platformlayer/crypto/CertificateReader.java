package org.platformlayer.crypto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.apache.log4j.Logger;
import org.platformlayer.ops.OpsException;

public class CertificateReader {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CertificateReader.class);

	public Certificate parse(byte[] data) throws OpsException {
		return parse(new ByteArrayInputStream(data));
	}

	public Certificate parse(InputStream data) throws OpsException {
		Certificate cert = null;
		try {
			cert = getX509CertificateFactory().generateCertificate(data);
		} catch (CertificateException ce) {
			throw new OpsException("Not an X509 certificate");
		}

		return cert;
	}

	public Certificate parse(String data) throws OpsException {
		return parse(data.getBytes());
	}

	CertificateFactory certificateFactory;

	private CertificateFactory getX509CertificateFactory() throws OpsException {
		if (certificateFactory == null) {
			try {
				certificateFactory = CertificateFactory.getInstance("X509");
			} catch (CertificateException e) {
				throw new OpsException("Error loading X509 provider", e);
			}
		}

		return certificateFactory;
	}

}
