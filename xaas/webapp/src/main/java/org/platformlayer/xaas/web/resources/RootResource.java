package org.platformlayer.xaas.web.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.model.RoleId;

@Path("/")
@Singleton
public class RootResource extends XaasResourceBase {
	static final Logger LOG = Logger.getLogger(RootResource.class);

	@Context
	HttpHeaders requestHeaders;

	@Context
	UriInfo uriInfo;

	@Context
	HttpServletRequest request;

	@Inject
	ObjectInjector objectInjector;

	public RootResource() {
		notifyRootResource();
	}

	@Path("{accountId}")
	public ServicesCollectionResource retrieveServiceList(@PathParam("accountId") String accountId) {
		checkIsInRole(RoleId.READ);

		ServicesCollectionResource resources = objectInjector.getInstance(ServicesCollectionResource.class);
		return resources;
	}

	@Override
	protected UriInfo getUriInfo() {
		return uriInfo;
	}

	@Override
	protected HttpServletRequest getRequest() {
		return request;
	}
}
