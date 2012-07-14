package org.platformlayer.xaas.web.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.xaas.AuthenticationCredentials;

import com.google.common.base.Objects;

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

	@Inject
	AuthenticationTokenValidator authTokenValidator;

	@Path("{projectId}")
	public ServicesCollectionResource retrieveServiceList(@PathParam("projectId") String projectKey) {
		ProjectAuthorization authz = authorizeProject(projectKey);

		getScope().put(new ProjectId(projectKey));
		getScope().put(ProjectAuthorization.class, authz);

		ServicesCollectionResource resources = objectInjector.getInstance(ServicesCollectionResource.class);
		return resources;
	}

	private ProjectAuthorization authorizeProject(String projectKey) {
		AuthenticationCredentials authn = getAuthenticationCredentials();
		if (authn == null) {
			throw new WebApplicationException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		ProjectAuthorization authz = null;
		if (authn instanceof ProjectAuthorization) {
			authz = (ProjectAuthorization) authn;
		} else {
			authz = authTokenValidator.validate(authn.getToken(), projectKey);
		}

		if (authz == null || !Objects.equal(authz.getName(), projectKey)) {
			throw new WebApplicationException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		return authz;
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
