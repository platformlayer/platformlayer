package org.platformlayer.ops.helpers;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
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
import org.openstack.crypto.CertificateAndKey;
import org.openstack.crypto.SimpleCertificateAndKey;
import org.platformlayer.Scope;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.client.PlatformLayerAuthAdminClient;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.crypto.BouncyCastleHelpers;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceConfiguration;

public class ProjectContext {
	@Inject
	OpsContext opsContext;

	@Inject
	PrivateDataHelper privateData;

	public static final ServiceMetadataKey METADATA_PROJECT_KEY = new ServiceMetadataKey("project.key");
	public static final ServiceMetadataKey METADATA_PROJECT_CERT = new ServiceMetadataKey("project.cert");

	public ProjectContext() {
	}

	public ProjectId getProjectId() {
		ServiceConfiguration serviceConfiguration = OpsContext.get().getServiceConfiguration();
		ProjectId project = serviceConfiguration.getProject();

		PlatformLayerKey targetItemKey = opsContext.getJobRecord().getTargetItemKey();
		if (targetItemKey != null) {
			ProjectId project2 = targetItemKey.getProject();
			if (!project.equals(project2)) {
				// Not sure yet which one we should be using if these differ!
				throw new IllegalStateException();
			}
		}

		return project;
	}

	public X500Principal getX500Principal() {
		return new X500Principal("CN=" + getProjectId().getKey());
	}

	public CertificateAndKey getProjectCredentials() throws OpsException {
		// OK... this is weird... we sign the project cert with the project cert.
		// It sort of makes sense, in that we don't want to share the project signing cert outside the auth server

		ProjectId projectId = getProjectId();

		KeyPair keyPair = privateData.findKeyPair(projectId, null, METADATA_PROJECT_KEY);
		List<X509Certificate> chain = privateData.findCertificate(projectId, null, METADATA_PROJECT_CERT);

		if (keyPair == null) {
			keyPair = RsaUtils.generateRsaKeyPair();
			privateData.putKeyPair(projectId, null, METADATA_PROJECT_KEY, keyPair);
		}

		if (chain == null) {
			AuthenticationTokenValidator authenticationTokenValidator = OpsContext.get().getInjector()
					.getInstance(AuthenticationTokenValidator.class);

			ProjectAuthorization projectAuthorization = Scope.get().get(ProjectAuthorization.class);
			String projectKey = projectAuthorization.getName();

			if (!projectKey.equals(projectId.getKey())) {
				throw new IllegalStateException();
			}

			PlatformLayerAuthAdminClient adminClient = (PlatformLayerAuthAdminClient) authenticationTokenValidator;
			String csr = buildCsr(keyPair, getX500Principal());
			chain = adminClient.signCsr(projectId.getKey(), projectAuthorization.getProjectSecret(), csr);

			privateData.putCertificate(projectId, null, METADATA_PROJECT_CERT, chain);
		}

		// privateData.getOrCreate(projectId, null, sshKeyName, user)
		// String sshKeyName = getSshKeyName();
		// return privateData.getOrCreate(getProjectId(), null, sshKeyName, "root");
		// return "project-" + getProjectId().getKey();
		//
		// KeyPair keyPair = projectContext.getSshKey().getKeyPair();
		// X500Principal subject = getX500Principal();

		return new SimpleCertificateAndKey(chain, keyPair.getPrivate());
	}

	private String buildCsr(KeyPair keyPair, X500Principal subjectName) {
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

		StringWriter stringWriter = new StringWriter();

		try {
			PemWriter writer = new PemWriter(stringWriter);
			PemObjectGenerator pemObject = new PemObject("CERTIFICATE REQUEST", csrHolder.getEncoded());
			writer.writeObject(pemObject);
			writer.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Error generating PEM", e);
		}

		return stringWriter.toString();

	}
}
