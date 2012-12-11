package org.platformlayer.ops.helpers;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.platformlayer.crypto.BouncyCastleHelpers;
import org.platformlayer.crypto.CsrParser;

public class Csr {
	final PKCS10CertificationRequest csr;

	private Csr(PKCS10CertificationRequest csr) {
		super();
		this.csr = csr;
	}

	public static Csr buildCsr(KeyPair keyPair, X500Principal subjectName) {
		X500Name subject = BouncyCastleHelpers.toX500Name(subjectName);
		SubjectPublicKeyInfo publicKeyInfo = BouncyCastleHelpers.toSubjectPublicKeyInfo(keyPair.getPublic());
		PKCS10CertificationRequestBuilder csrBuilder = new PKCS10CertificationRequestBuilder(subject, publicKeyInfo);

		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

		BcRSAContentSignerBuilder sigBuild = new BcRSAContentSignerBuilder(sigAlgId, digAlgId);
		ContentSigner signer;
		try {
			signer = sigBuild.build(BouncyCastleHelpers.toAsymmetricKeyParameter(keyPair.getPrivate()));
		} catch (OperatorCreationException e) {
			throw new IllegalArgumentException("Error building content signer", e);
		}

		PKCS10CertificationRequest csrHolder = csrBuilder.build(signer);

		return new Csr(csrHolder);
	}

	public static Csr parse(String encoded) {
		CsrParser parser = new CsrParser();
		PKCS10CertificationRequest csr = parser.parse(encoded);
		if (csr == null) {
			throw new IllegalArgumentException("Cannot parse CSR");
		}

		return new Csr(csr);
	}

	public String getSubject() {
		return csr.getSubject().toString();
	}

	public String getEncoded() {
		StringWriter stringWriter = new StringWriter();

		try {
			PemWriter writer = new PemWriter(stringWriter);
			PemObjectGenerator pemObject = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());
			writer.writeObject(pemObject);
			writer.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error generating PEM", e);
		}

		return stringWriter.toString();
	}

}
