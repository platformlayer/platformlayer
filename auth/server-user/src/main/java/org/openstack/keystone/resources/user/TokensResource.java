package org.openstack.keystone.resources.user;

import java.security.cert.X509Certificate;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Access;
import org.openstack.keystone.model.Auth;
import org.openstack.keystone.model.AuthenticateRequest;
import org.openstack.keystone.model.AuthenticateResponse;
import org.openstack.keystone.model.CertificateCredentials;
import org.openstack.keystone.model.PasswordCredentials;
import org.openstack.keystone.model.Token;
import org.openstack.keystone.resources.KeystoneResourceBase;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.TokenInfo;
import org.openstack.keystone.services.TokenService;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;

@Path("/v2.0/tokens")
public class TokensResource extends KeystoneResourceBase {
	static final Logger log = Logger.getLogger(TokensResource.class);

	// @Inject
	// ServiceMapper serviceMapper;

	@Inject
	TokenService tokenService;

	@GET
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	public AuthenticateResponse authenticateGET() {
		String username = request.getParameter("user");
		String password = request.getParameter("password");
		String project = request.getParameter("project");

		AuthenticateRequest request = new AuthenticateRequest();
		request.auth = new Auth();
		request.auth.project = project;

		if (password != null) {
			PasswordCredentials credentials = new PasswordCredentials();

			credentials.username = username;
			credentials.password = password;

			request.auth.passwordCredentials = credentials;
		} else {
			CertificateCredentials credentials = new CertificateCredentials();

			credentials.username = username;

			request.auth.certificateCredentials = credentials;
		}

		return authenticate(request);
	}

	@POST
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	public AuthenticateResponse authenticatePOST(AuthenticateRequest request) {
		if (request.auth == null) {
			throwUnauthorized();
		}

		return authenticate(request);
	}

	private AuthenticateResponse authenticate(AuthenticateRequest request) {
		AuthenticateResponse response = new AuthenticateResponse();

		String username = null;
		String projectKey = request.auth.project;

		UserEntity user = null;
		ProjectEntity project = null;

		if (request.auth.passwordCredentials != null) {
			username = request.auth.passwordCredentials.username;
			String password = request.auth.passwordCredentials.password;

			try {
				user = userAuthenticator.authenticate(projectKey, username, password);
			} catch (AuthenticatorException e) {
				// An exception indicates something went wrong (i.e. not just bad credentials)
				log.warn("Error while getting user info", e);
				throwInternalError();
			}
		} else if (request.auth.certificateCredentials != null) {
			username = request.auth.certificateCredentials.username;

			X509Certificate[] certificateChain = getCertificateChain();
			if (certificateChain == null) {
				throwUnauthorized();
			}

			byte[] challengeResponse = request.auth.certificateCredentials.challengeResponse;

			CertificateAuthenticationRequest details = new CertificateAuthenticationRequest();
			details.certificateChain = certificateChain;
			details.username = username;
			details.projectKey = projectKey;
			details.challengeResponse = challengeResponse;

			CertificateAuthenticationResponse result = null;
			try {
				result = userAuthenticator.authenticate(details);
			} catch (AuthenticatorException e) {
				log.warn("Error while getting authenticating by certificate", e);
				throwInternalError();
			}

			if (result == null) {
				throwUnauthorized();
			}

			if (challengeResponse != null) {
				if (result.user == null || result.project == null) {
					throwUnauthorized();
				}

				user = (UserEntity) result.user;
				project = (ProjectEntity) result.project;
			} else {
				log.debug("Returning authentication challenge for user: " + username);

				response.challenge = result.challenge;
				return response;
			}
		} else {
			throwUnauthorized();
		}

		if (user == null) {
			log.debug("Authentication request failed.  Username=" + username);
			throwUnauthorized();
		}

		if (projectKey != null) {
			if (project == null) {
				try {
					project = userAuthenticator.findProject(projectKey, user);
				} catch (AuthenticatorException e) {
					log.warn("Error while getting project info", e);
					throwInternalError();
				}
			}

			// If we are doing a scope auth, make sure we have access
			if (project == null) {
				throwUnauthorized();
			}
		}

		log.debug("Successful authentication for user: " + user.key);

		TokenInfo token = buildToken(projectKey, "" + user.getId(), user.getTokenSecret());

		response.access = new Access();
		// response.access.serviceCatalog = serviceMapper.getServices(userInfo, project);
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

}
