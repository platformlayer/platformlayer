package org.platformlayer.auth.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SignCertificateRequest {
	public String project;
	public String csr;
	public byte[] projectSecret;
}
