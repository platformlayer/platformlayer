package org.platformlayer.auth.client;

import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.openstack.crypto.Md5Hash;
import org.openstack.utils.Hex;
import org.platformlayer.auth.v1.CertificateChainInfo;
import org.platformlayer.auth.v1.CertificateInfo;
import org.platformlayer.crypto.OpenSshUtils;

public class CertificateChains {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CertificateChains.class);

	public static CertificateChainInfo toModel(X509Certificate[] chain) {
		CertificateChainInfo chainInfo = new CertificateChainInfo();
		List<CertificateInfo> certificates = chainInfo.getCertificates();
		for (X509Certificate cert : chain) {
			CertificateInfo certificateInfo = new CertificateInfo();

			certificateInfo.setSubjectDN(cert.getSubjectX500Principal().getName());
			Md5Hash hash = OpenSshUtils.getSignature(cert.getPublicKey());
			certificateInfo.setPublicKeyHash(hash.toHex());

			byte[] data = cert.getPublicKey().getEncoded();
			certificateInfo.setPublicKey(Hex.toHex(data));

			certificates.add(certificateInfo);
		}

		return chainInfo;
	}
}
