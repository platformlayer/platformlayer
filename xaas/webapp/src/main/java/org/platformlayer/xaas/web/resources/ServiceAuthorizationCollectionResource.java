package org.platformlayer.xaas.web.resources;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.platformlayer.ids.ServiceType;

public class ServiceAuthorizationCollectionResource extends XaasResourceBase {
	@Path("{serviceType}")
	public ServiceAuthorizationResource getOne(@PathParam("serviceType") String serviceType) {
		getScope().put(new ServiceType(serviceType));

		ServiceAuthorizationResource resource = objectInjector.getInstance(ServiceAuthorizationResource.class);
		return resource;
	}
}
