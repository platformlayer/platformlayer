package org.openstack.keystone.resources.user;

import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Access;
import org.openstack.keystone.model.AuthenticateRequest;
import org.openstack.keystone.model.AuthenticateResponse;
import org.openstack.keystone.model.Token;
import org.openstack.keystone.resources.KeystoneResourceBase;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.TokenService;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

@Path("/v2.0/tokens")
public class TokensResource extends KeystoneResourceBase {
	static final Logger log = Logger.getLogger(TokensResource.class);

	// @Inject
	// ServiceMapper serviceMapper;

	@Inject
	TokenService tokenService;

	@POST
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	public AuthenticateResponse authenticate(AuthenticateRequest request) {
		if (request.auth == null) {
			throwUnauthorized();
		}

		String username = null;
		String password = null;
		String project = request.auth.project;

		if (request.auth.passwordCredentials != null) {
			username = request.auth.passwordCredentials.username;
			password = request.auth.passwordCredentials.password;
		}

		// return buildToken(project, "" + user.getId(), user.getTokenSecret());
		// }
		//
		// TokenInfo tokenInfo = null;
		// try {
		// tokenInfo = tryAuthenticate(request.auth);
		// } catch (Exception e) {
		// // An exception indicates something went wrong (i.e. not just bad credentials)
		// log.warn("Error while authenticating", e);
		// throwInternalError();
		// }
		//
		// if (tokenInfo == null) {
		// throwUnauthorized();
		// }

		// String project = request.auth.project;

		UserEntity user = null;
		try {
			user = userAuthenticator.authenticate(username, password);
		} catch (AuthenticatorException e) {
			// An exception indicates something went wrong (i.e. not just bad credentials)
			log.warn("Error while getting user info", e);
			throwInternalError();
		}

		if (user == null) {
			log.debug("Authentication request failed for " + username);
			return null;
		}

		AuthenticateResponse response = new AuthenticateResponse();
		response.access = new Access();
		// response.access.serviceCatalog = serviceMapper.getServices(userInfo, project);

		if (project != null) {
			ProjectEntity projectEntity = null;
			try {
				projectEntity = userAuthenticator.findProject(project, user);
			} catch (AuthenticatorException e) {
				log.warn("Error while getting project info", e);
				throwInternalError();
			}
			// If we are doing a scope auth, make sure we have access
			if (projectEntity == null) {
				throwUnauthorized();
			}
		}

		log.debug("Successful authentication for user: " + user.key);

		TokenInfo token = buildToken(project, "" + user.getId(), user.getTokenSecret());

		response.access.token = new Token();
		response.access.token.expires = token.expiration;
		response.access.token.id = tokenService.encodeToken(token);

		return response;
	}

	private TokenInfo buildToken(String project, String userId, byte[] tokenSecret) {
		Date now = new Date();
		Date expiration = TOKEN_VALIDITY.addTo(now);

		byte flags = 0;
		TokenInfo tokenInfo = new TokenInfo(flags, project, userId, expiration, tokenSecret);

		return tokenInfo;
	}

	// @GET
	// @Produces({ APPLICATION_JSON, APPLICATION_XML })
	// public TenantsList listTenants(@HeaderParam(AUTH_HEADER) String token) {
	// // TODO: What is this call for?
	//
	// TokenInfo tokenInfo = authentication.validateToken(token);
	// if (tokenInfo == null) {
	// throwUnauthorized();
	// }
	//
	// TenantsList response = new TenantsList();
	// response.tenant = Lists.newArrayList();
	//
	// String scope = tokenInfo.scope;
	// if (scope == null) {
	// // Unscoped token; no tenant access
	// } else {
	// Tenant tenant = new Tenant();
	// tenant.id = scope;
	// tenant.enabled = true;
	// tenant.name = scope;
	// response.tenant.add(tenant);
	// }
	//
	// return response;
	// }

}
