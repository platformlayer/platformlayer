package org.platformlayer.auth.system;

import java.security.cert.X509Certificate;
import java.util.List;

import org.platformlayer.auth.v1.CertificateChainInfo;
import org.platformlayer.auth.v1.CertificateInfo;
import org.platformlayer.crypto.OpenSshUtils;

import com.fathomdb.hash.Md5Hash;
import com.fathomdb.utils.Hex;

public class CertificateChains {
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
