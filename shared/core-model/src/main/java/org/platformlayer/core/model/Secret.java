package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

import org.platformlayer.codegen.GwtSafe;

@XmlAccessorType(XmlAccessType.NONE)
@GwtSafe
public class Secret {
	@XmlValue
	public String value;

	public Secret() {
	}

	@Deprecated
	// For JAXB
	public Secret(String s) {
		this.value = s;
	}

	public static Secret build(String s) {
		Secret secret = new Secret(s);
		return secret;
	}

	public static boolean isNullOrEmpty(Secret secret) {
		if (secret == null) {
			return true;
		}
		if (secret.value == null) {
			return true;
		}
		return secret.value.isEmpty();
	}

	public String plaintext() {
		return value;
	}
}
