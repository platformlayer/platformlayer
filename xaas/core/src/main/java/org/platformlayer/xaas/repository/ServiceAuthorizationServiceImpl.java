package org.platformlayer.xaas.repository;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceMetadataKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.model.ServiceAuthorization;

public class ServiceAuthorizationServiceImpl implements ServiceAuthorizationService {

    @Inject
    ServiceAuthorizationRepository repository;

    @Override
    public ServiceAuthorization findServiceAuthorization(ServiceType serviceType, ProjectId projectId) throws RepositoryException {
        ServiceAuthorization serviceAuthorization = repository.findServiceAuthorization(serviceType, projectId);
        if (serviceAuthorization == null) {
        }

        return serviceAuthorization;
    }

    @Override
    public ServiceAuthorization createAuthorization(ProjectId projectId, ServiceAuthorization authorization) throws RepositoryException {
        return repository.createAuthorization(projectId, authorization);
    }

    @Override
    public String findPrivateData(ServiceType serviceType, ProjectId projectId, ServiceMetadataKey key) throws RepositoryException {
        return repository.findPrivateData(serviceType, projectId, key);
    }

    @Override
    public void setPrivateData(ServiceType serviceType, ProjectId projectId, ServiceMetadataKey key, String value) throws RepositoryException {
        repository.setPrivateData(serviceType, projectId, key, value);
    }

}
