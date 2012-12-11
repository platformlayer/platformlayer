package org.platformlayer.ops.helpers;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import org.platformlayer.Scope;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.system.PlatformLayerAuthAdminClient;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceConfiguration;

import com.fathomdb.crypto.CertificateAndKey;
import com.fathomdb.crypto.SimpleCertificateAndKey;

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

			PlatformLayerAuthAdminClient adminClient = PlatformLayerAuthAdminClient.find(authenticationTokenValidator);
			Csr csr = Csr.buildCsr(keyPair, getX500Principal());
			chain = adminClient.signCsr(projectId.getKey(), projectAuthorization.getProjectSecret(), csr.getEncoded());

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

}
