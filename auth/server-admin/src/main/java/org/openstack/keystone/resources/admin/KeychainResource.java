package org.openstack.keystone.resources.admin;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.UserProjectEntity;
import org.platformlayer.auth.model.CertificateChainInfo;
import org.platformlayer.auth.model.ValidateAccess;
import org.platformlayer.auth.model.ValidateTokenResponse;
import org.platformlayer.auth.resources.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("v2.0/keychain")
public class KeychainResource extends RootResource {
	private static final Logger log = LoggerFactory.getLogger(KeychainResource.class);

	@POST
	public ValidateTokenResponse authorizeCertificateChain(@QueryParam("project") String project,
			CertificateChainInfo chain) {
		try {
			requireSystemAccess();
		} catch (AuthenticatorException e) {
			log.warn("Error while checking system token", e);
			throwInternalError();
		}

		UserEntity userEntity = null;
		try {
			boolean unlock = false;
			userEntity = userAuthenticator.findUserFromKeychain(chain, unlock);
		} catch (AuthenticatorException e) {
			log.warn("Error while fetching user", e);
			throwInternalError();
		}

		if (userEntity == null) {
			throw404NotFound();
		}

		ValidateTokenResponse response = new ValidateTokenResponse();
		response.access = new ValidateAccess();
		response.access.user = Mapping.mapToUserValidation(userEntity);

		// response.access.token = new Token();
		// response.access.token.expires = checkTokenInfo.expiration;
		// response.access.token.id = checkToken;

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

			// Note that we do not unlock the user / project; we don't have any secret material
			// TODO: We could return stuff encrypted with the user's public key
			// projectEntity.unlockWithUser(userEntity);
			//
			// if (!projectEntity.isSecretValid()) {
			// throw404NotFound();
			// }

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
