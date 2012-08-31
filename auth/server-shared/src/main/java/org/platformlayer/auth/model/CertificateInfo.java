package org.platformlayer.auth.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class CertificateInfo {
	public String publicKeyHash;
	public String publicKey;
	public String subjectDN;

	@Override
	public String toString() {
		return "CertificateInfo [subjectDN=" + subjectDN + ", publicKey=" + publicKey + ", publicKeyHash="
				+ publicKeyHash + "]";
	}

}
