package org.openstack.keystone.resources.admin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.UserProjectEntity;
import org.platformlayer.auth.model.Token;
import org.platformlayer.auth.model.ValidateAccess;
import org.platformlayer.auth.model.ValidateTokenResponse;
import org.platformlayer.auth.resources.Mapping;
import org.platformlayer.auth.services.TokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("v2.0/tokens")
public class TokensResource extends RootResource {
	private static final Logger log = LoggerFactory.getLogger(TokensResource.class);

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

		UserEntity userEntity = null;
		try {
			userEntity = userAuthenticator.getUserFromToken(checkTokenInfo.userId, checkTokenInfo.tokenSecret);
		} catch (AuthenticatorException e) {
			log.warn("Error while fetching user", e);
			throwInternalError();
		}

		ValidateTokenResponse response = new ValidateTokenResponse();
		response.access = new ValidateAccess();
		response.access.user = Mapping.mapToUserValidation(userEntity);

		response.access.token = new Token();
		response.access.token.expires = checkTokenInfo.expiration;
		response.access.token.id = checkToken;

		String checkProject = project;

		if (checkProject != null) {
			ProjectEntity projectEntity = null;

			try {
				projectEntity = userAuthenticator.findProject(checkProject);
			} catch (AuthenticatorException e) {
				log.warn("Error while fetching project", e);
				throwInternalError();
			}

			if (projectEntity == null) {
				throw404NotFound();
			}

			projectEntity.unlockWithUser(userEntity);

			if (!projectEntity.isSecretValid()) {
				throw404NotFound();
			}

			UserProjectEntity userProject = null;
			try {
				userProject = userAuthenticator.findUserProject(userEntity, projectEntity);
			} catch (AuthenticatorException e) {
				log.warn("Error while fetching project", e);
				throwInternalError();
			}

			if (userProject == null) {
				// Not a member of project
				throw404NotFound();
			}

			response.access.project = Mapping.mapToProject(projectEntity);
			response.access.project.roles = Mapping.mapToRoles(userProject.getRoles());
		}

		return response;
	}

}
