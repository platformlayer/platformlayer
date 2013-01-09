package org.platformlayer.xaas.web.resources;

import java.util.List;

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

import org.slf4j.*;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.inject.ObjectInjector;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.model.RoleId;
import org.platformlayer.web.AuthenticationFilter;

@Path("/")
@Singleton
public class RootResource extends XaasResourceBase {
	static final Logger log = LoggerFactory.getLogger(RootResource.class);

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

	@Inject
	BlobsResource blobs;

	@Path("_blobs")
	public BlobsResource getBlobs() {
		return blobs;
	}

	@Path("{projectId}")
	public ServicesCollectionResource retrieveServiceList(@PathParam("projectId") String projectKey) {
		ProjectAuthorization authz = AuthenticationFilter.authorizeProject(getAuthenticationCredentials(),
				authTokenValidator, projectKey);
		if (authz == null) {
			throw new WebApplicationException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		List<RoleId> roles = authz.getRoles();
		if (roles == null || !roles.contains(RoleId.OWNER)) {
			throw new WebApplicationException(HttpServletResponse.SC_UNAUTHORIZED);
		}

		getScope().put(new ProjectId(projectKey));
		getScope().put(ProjectAuthorization.class, authz);

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
