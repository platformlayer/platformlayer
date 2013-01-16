package org.platformlayer.ops.helpers;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.*;
import org.platformlayer.ApplicationMode;
import org.platformlayer.RepositoryException;
import org.platformlayer.crypto.CertificateUtils;
import org.platformlayer.crypto.KeyPairUtils;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.repository.ServiceAuthorizationService;

import com.fathomdb.crypto.OpenSshUtils;
import com.fathomdb.io.IoUtils;

public class PrivateDataHelper {
	private static final Logger log = LoggerFactory.getLogger(PrivateDataHelper.class);

	@Inject
	ServiceAuthorizationService serviceAuthorizationService;

	public static final ServiceMetadataKey METADATA_SSHKEY = new ServiceMetadataKey("sshkey");

	public String findData(ProjectId projectId, ServiceType serviceType, ServiceMetadataKey keyId) throws OpsException {
		String keyData;
		try {
			keyData = serviceAuthorizationService.findPrivateData(serviceType, projectId, keyId);
		} catch (RepositoryException e) {
			throw new OpsException("Error reading from repository", e);
		}
		return keyData;
	}

	public KeyPair findKeyPair(ProjectId projectId, ServiceType serviceType, ServiceMetadataKey keyId)
			throws OpsException {
		String keyData = findData(projectId, serviceType, keyId);
		if (keyData == null) {
			return null;
		}
		try {
			KeyPair keyPair = KeyPairUtils.deserialize(keyData);

			saveKeypairInDevelopment(projectId, serviceType, keyPair);

			return keyPair;
		} catch (IOException e) {
			throw new OpsException("Error deserializing SSH key", e);
		}
	}

	public List<X509Certificate> findCertificate(ProjectId projectId, ServiceType serviceType, ServiceMetadataKey keyId)
			throws OpsException {
		String data = findData(projectId, serviceType, keyId);
		if (data == null) {
			return null;
		}

		try {
			List<X509Certificate> cert = CertificateUtils.fromPem(data);

			// saveKeypairInDevelopment(projectId, serviceType, keyPair);

			return cert;
		} catch (IllegalArgumentException e) {
			throw new OpsException("Error deserializing certificate", e);
		}
	}

	public void putKeyPair(ProjectId project, ServiceType serviceType, ServiceMetadataKey keyId, KeyPair keyPair)
			throws OpsException {
		String serialized;
		try {
			serialized = KeyPairUtils.serialize(keyPair);
		} catch (IOException e) {
			throw new OpsException("Error serializing key pair", e);
		}
		try {
			serviceAuthorizationService.setPrivateData(serviceType, project, keyId, serialized);
		} catch (RepositoryException e) {
			throw new OpsException("Error writing to repository", e);
		}

		saveKeypairInDevelopment(project, serviceType, keyPair);
	}

	public void putCertificate(ProjectId projectId, ServiceType serviceType, ServiceMetadataKey keyId,
			List<X509Certificate> chain) throws OpsException {
		String serialized;
		try {
			serialized = CertificateUtils.toPem(chain);
		} catch (IllegalArgumentException e) {
			throw new OpsException("Error serializing certificate", e);
		}
		try {
			serviceAuthorizationService.setPrivateData(serviceType, projectId, keyId, serialized);
		} catch (RepositoryException e) {
			throw new OpsException("Error writing to repository", e);
		}

		// saveKeypairInDevelopment(project, serviceType, cert);
	}

	private void saveKeypairInDevelopment(ProjectId project, ServiceType serviceType, KeyPair keyPair)
			throws OpsException {
		if (ApplicationMode.isDevelopment()) {
			String fileName;

			if (serviceType != null) {
				fileName = "service-" + serviceType.getKey();
			} else {
				fileName = "project-" + project.getKey();
			}

			File credentials = IoUtils.resolve("~/.credentials");
			File ssh = new File(credentials, "ssh");
			File projectDir = new File(ssh, project.getKey());
			File keyFile = new File(projectDir, fileName);

			if (!keyFile.exists()) {
				projectDir.mkdirs();

				log.warn("Writing SSH key to " + keyFile);

				try {
					String serialized = KeyPairUtils.serialize(keyPair);
					IoUtils.writeAll(keyFile, serialized);
				} catch (IOException e) {
					throw new OpsException("Error serializing SSH key", e);
				}
			}

			String sshPublicKey = OpenSshUtils.serialize(keyPair.getPublic());
			log.info("SSH public key for " + serviceType + ":" + project + " is " + sshPublicKey);
		}
	}

	public KeyPair findSshKey(ProjectId projectId, ServiceType serviceType) throws OpsException {
		return findKeyPair(projectId, serviceType, METADATA_SSHKEY);
	}

	private void storeSshKeyPair(ProjectId projectId, ServiceType serviceType, KeyPair sshKeyPair) throws OpsException {
		putKeyPair(projectId, serviceType, METADATA_SSHKEY, sshKeyPair);
	}

	public SshKey getOrCreate(ProjectId projectId, ServiceType serviceType, String sshKeyName, String user)
			throws OpsException {
		KeyPair keyPair = findSshKey(projectId, serviceType);
		if (keyPair == null) {
			keyPair = RsaUtils.generateRsaKeyPair();
			// sshKeyPair = cloud.generateSshKeyPair(sshKeyName);
			storeSshKeyPair(projectId, serviceType, keyPair);
		}

		return new SshKey(sshKeyName, user, keyPair);
	}

}
