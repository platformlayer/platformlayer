package org.platformlayer.auth.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Auth {
	public PasswordCredentials passwordCredentials;

	public CertificateCredentials certificateCredentials;

	// @XmlAttribute
	// public String project;

	public Token token;

	@XmlElement(name = "user")
	public UserValidation userValidation;
}
