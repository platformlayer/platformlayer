package org.openstack.keystone.resources.admin;

import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Token;
import org.openstack.keystone.model.ValidateAccess;
import org.openstack.keystone.model.ValidateTokenResponse;
import org.openstack.keystone.resources.KeystoneResourceBase;
import org.openstack.keystone.resources.Mapping;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.ServiceAccount;
import org.openstack.keystone.services.SystemAuthenticator;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.TokenService;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

@Path("v2.0/tokens")
public class TokensResource extends KeystoneResourceBase {
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
	public ValidateTokenResponse validateToken(@PathParam("tokenId") String checkToken) {
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

		if (checkTokenInfo.project != null) {
			ProjectEntity projectEntity = null;

			try {
				projectEntity = userAuthenticator.findProject(checkTokenInfo.project, userInfo);
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
