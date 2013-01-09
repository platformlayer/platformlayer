package org.platformlayer.ops;

import org.slf4j.*;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;

public class ServiceConfiguration {
	static final Logger log = LoggerFactory.getLogger(ServiceConfiguration.class);

	final ServiceType serviceType;
	final ProjectId project;

	public ServiceConfiguration(ProjectId project, ServiceType serviceType) {
		this.serviceType = serviceType;
		this.project = project;
	}

	public ProjectId getProject() {
		return project;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}
}
