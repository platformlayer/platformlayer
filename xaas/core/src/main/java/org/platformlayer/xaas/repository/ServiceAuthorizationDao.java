package org.platformlayer.xaas.repository;

import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xaas.model.ServiceAuthorization;

public interface ServiceAuthorizationDao {
    // TODO: Do we need a separate DAO and repository?
    ServiceAuthorization getServiceAuthorization(ServiceType serviceType, ProjectId project);

    ServiceAuthorization createAuthorization(ProjectId project, ServiceAuthorization authorization);
}
