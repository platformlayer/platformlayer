package org.platformlayer.xaas.repository;

import org.platformlayer.RepositoryException;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.model.ServiceAuthorization;

import com.google.inject.ImplementedBy;

@ImplementedBy(ServiceAuthorizationServiceImpl.class)
public interface ServiceAuthorizationService {
	ServiceAuthorization findServiceAuthorization(ServiceType serviceType, ProjectId projectId)
			throws RepositoryException;

	ServiceAuthorization createAuthorization(ProjectId projectId, ServiceAuthorization authorization)
			throws RepositoryException;

	String findPrivateData(ServiceType serviceType, ProjectId projectId, ServiceMetadataKey key)
			throws RepositoryException;

	void setPrivateData(ServiceType serviceType, ProjectId projectId, ServiceMetadataKey key, String value)
			throws RepositoryException;

}
