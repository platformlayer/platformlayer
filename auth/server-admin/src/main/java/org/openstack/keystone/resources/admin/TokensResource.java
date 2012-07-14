package org.openstack.keystone.resources.admin;

import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.ServiceAccount;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.model.Token;
import org.platformlayer.auth.model.ValidateAccess;
import org.platformlayer.auth.model.ValidateTokenResponse;
import org.platformlayer.auth.resources.PlatformlayerAuthResourceBase;
import org.platformlayer.auth.resources.Mapping;
import org.platformlayer.auth.services.SystemAuthenticator;
import org.platformlayer.auth.services.TokenInfo;
import org.platformlayer.auth.services.TokenService;

@Path("v2.0/tokens")
public class TokensResource extends PlatformlayerAuthResourceBase {
	static final Logger log = Logger.getLogger(TokensResource.class);

	@Inject
	TokenService tokenService;

	@Inject
	SystemAuthenticator systemAuthenticator;

	protected void requireSystemAccess() throws AuthenticatorException {
		X509Certificate[] certChain = getCertificateChain();
		if (certChain != null && certChain.length != 0) {
			X509Certificate head = certChain[0];

			ServiceAccount auth = systemAuthenticator.authenticate(certChain);
			if (auth != null) {
				return;
			}

			log.debug("Certificate authentication request failed for " + head);
		}

		throwUnauthorized();

		// return myTokenInfo;
	}

	@GET
	// @HEAD support is automatic from the @GET
	@Path("{tokenId}")
	public ValidateTokenResponse validateToken(@PathParam("tokenId") String checkToken,
			@QueryParam("project") String project) {
		try {
			requireSystemAccess();
		} catch (AuthenticatorException e) {
			log.warn("Error while checking system token", e);
			throwInternalError();
		}

		TokenInfo checkTokenInfo = tokenService.decodeToken(checkToken);
		if (checkTokenInfo == null || checkTokenInfo.hasExpired()) {
			throw404NotFound();
		}

		// if (project != null) {
		// if (!Objects.equal(project, checkTokenInfo.project)) {
		// throw404NotFound();
		// }
		// }

		UserEntity userInfo = null;
		try {
			userInfo = userAuthenticator.getUserFromToken(checkTokenInfo.userId, checkTokenInfo.tokenSecret);
		} catch (AuthenticatorException e) {
			log.warn("Error while fetching user", e);
			throwInternalError();
		}

		ValidateTokenResponse response = new ValidateTokenResponse();
		response.access = new ValidateAccess();
		response.access.user = Mapping.mapToUserValidation(userInfo);

		response.access.token = new Token();
		response.access.token.expires = checkTokenInfo.expiration;
		response.access.token.id = checkToken;

		String checkProject = project;

		if (checkProject != null) {
			ProjectEntity projectEntity = null;

			try {
				projectEntity = userAuthenticator.findProject(checkProject, userInfo);
			} catch (AuthenticatorException e) {
				log.warn("Error while fetching project", e);
				throwInternalError();
			}
			if (projectEntity == null) {
				throw404NotFound();
			}

			response.access.project = Mapping.mapToProject(projectEntity);
		}

		return response;
	}
}
