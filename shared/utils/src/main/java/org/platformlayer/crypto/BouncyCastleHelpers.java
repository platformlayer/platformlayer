package org.platformlayer.crypto;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.X509Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BouncyCastleHelpers {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BouncyCastleHelpers.class);

	public static X500Name toX500Name(X500Principal principal) {
		try {
			X509Principal x509 = new X509Principal(principal.getEncoded());
			return X500Name.getInstance(x509);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting to X500", e);
		}
	}

	public static SubjectPublicKeyInfo toSubjectPublicKeyInfo(PublicKey publicKey) {
		try {
			return SubjectPublicKeyInfo.getInstance(new ASN1InputStream(publicKey.getEncoded()).readObject());
		} catch (IOException e) {
			throw new IllegalArgumentException("Error converting to SubjectPublicKeyInfo", e);
		}
	}

	public static AsymmetricKeyParameter toAsymmetricKeyParameter(PrivateKey privateKey) {
		try {
			AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(privateKey.getEncoded());
			return asymmetricKeyParameter;
		} catch (IOException e) {
			throw new IllegalArgumentException("Error parsing private key", e);
		}
	}

}
