package org.platformlayer.ops;

import java.io.IOException;
import java.security.KeyPair;

import org.platformlayer.KeyPairUtils;
import org.platformlayer.RepositoryException;
import org.platformlayer.crypto.RsaUtils;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.repository.ServiceAuthorizationService;

public class ServiceConfiguration {
	private static final ServiceMetadataKey METADATA_SSHKEY = new ServiceMetadataKey("sshkey");

	final ServiceAuthorizationService serviceAuthorizationService;

	final ServiceType serviceType;
	private final ProjectId project;

	public ServiceConfiguration(ServiceAuthorizationService serviceAuthorizationService, ServiceType serviceType,
			ProjectId project) {
		this.serviceAuthorizationService = serviceAuthorizationService;
		this.serviceType = serviceType;
		this.project = project;
	}

	public ProjectId getProject() {
		return project;
	}

	public KeyPair findSshKey() throws OpsException {
		return findSshKey(serviceType);
	}

	public KeyPair findKeyPair(ServiceMetadataKey keyId, int createLength) throws OpsException {
		ProjectId project = getProject();
		KeyPair keyPair = findKeyPair(serviceType, project, keyId);
		if (createLength > 0 && keyPair == null) {
			keyPair = RsaUtils.generateRsaKeyPair(createLength);
			storeKeyPair(serviceType, project, keyId, keyPair);
		}
		return keyPair;
	}

	@Deprecated
	public KeyPair findSshKey(ServiceType serviceType) throws OpsException {
		return findKeyPair(serviceType, getProject(), METADATA_SSHKEY);
	}

	private KeyPair findKeyPair(ServiceType serviceType, ProjectId projectId, ServiceMetadataKey keyId)
			throws OpsException {
		String keyData;
		try {
			keyData = serviceAuthorizationService.findPrivateData(serviceType, projectId, keyId);
		} catch (RepositoryException e) {
			throw new OpsException("Error reading from repository", e);
		}
		if (keyData == null) {
			return null;
		}
		try {
			KeyPair keyPair = KeyPairUtils.deserialize(keyData);
			return keyPair;
		} catch (IOException e) {
			throw new OpsException("Error deserializing SSH key", e);
		}
	}

	public void storeSshKeyPair(KeyPair sshKeyPair) throws OpsException {
		storeKeyPair(serviceType, getProject(), METADATA_SSHKEY, sshKeyPair);
	}

	private void storeKeyPair(ServiceType serviceType, ProjectId project, ServiceMetadataKey keyId, KeyPair keyPair)
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
	}

	public ServiceAuthorization findServiceAuthorization() throws RepositoryException {
		return serviceAuthorizationService.findServiceAuthorization(serviceType, getProject());
	}

	public ServiceType getServiceType() {
		return serviceType;
	}
}
