package org.openstack.keystone.resources.user;

import java.security.cert.X509Certificate;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.model.AuthenticateRequest;
import org.platformlayer.auth.model.AuthenticateResponse;

@Path("/v2.0/tokens")
public class TokensResource extends UserResourceBase {
	static final Logger log = Logger.getLogger(TokensResource.class);

	// @GET
	// @Produces({ JSONP })
	// public JSONWithPadding authenticateGET(@QueryParam("callback") String jsonCallback) {
	// String username = request.getParameter("user");
	// String password = request.getParameter("password");
	// // String project = request.getParameter("project");
	//
	// AuthenticateRequest request = new AuthenticateRequest();
	// request.auth = new Auth();
	//
	// if (password != null) {
	// PasswordCredentials credentials = new PasswordCredentials();
	//
	// credentials.username = username;
	// credentials.password = password;
	//
	// request.auth.passwordCredentials = credentials;
	// } else {
	// CertificateCredentials credentials = new CertificateCredentials();
	//
	// credentials.username = username;
	//
	// request.auth.certificateCredentials = credentials;
	// }
	//
	// AuthenticateResponse authenticateResponse = null;
	// try {
	// authenticateResponse = authenticate(request);
	// } catch (WebApplicationException e) {
	// authenticateResponse = new AuthenticateResponse();
	// authenticateResponse.statusCode = e.getResponse().getStatus();
	// } catch (Exception e) {
	// log.info("Reporting exception as 500", e);
	//
	// authenticateResponse = new AuthenticateResponse();
	// authenticateResponse.statusCode = 500;
	// }
	//
	// if (authenticateResponse == null) {
	// authenticateResponse = new AuthenticateResponse();
	// authenticateResponse.statusCode = 401;
	// }
	//
	// JSONWithPadding jsonWithPadding = new JSONWithPadding(authenticateResponse, jsonCallback);
	// return jsonWithPadding;
	// }

	@POST
	@Produces({ APPLICATION_JSON, APPLICATION_XML })
	@Consumes({ APPLICATION_JSON, APPLICATION_XML })
	public AuthenticateResponse authenticatePOST(AuthenticateRequest request) {
		if (request.auth == null) {
			throwUnauthorized();
		}

		AuthenticateResponse response = authenticate(request);
		if (response == null) {
			throwUnauthorized();
		}

		return response;
	}

	private AuthenticateResponse authenticate(AuthenticateRequest request) {
		AuthenticateResponse response = new AuthenticateResponse();

		String username = null;

		UserEntity user = null;

		if (request.auth.passwordCredentials != null) {
			username = request.auth.passwordCredentials.username;
			String password = request.auth.passwordCredentials.password;

			try {
				user = userAuthenticator.authenticate(username, password);
			} catch (AuthenticatorException e) {
				// An exception indicates something went wrong (i.e. not just bad credentials)
				log.warn("Error while getting user info", e);
				throwInternalError();
			}
		} else if (request.auth.certificateCredentials != null) {
			username = request.auth.certificateCredentials.username;

			X509Certificate[] certificateChain = getCertificateChain();
			if (certificateChain == null) {
				return null;
			}

			byte[] challengeResponse = request.auth.certificateCredentials.challengeResponse;

			CertificateAuthenticationRequest details = new CertificateAuthenticationRequest();
			details.certificateChain = certificateChain;
			details.username = username;
			// details.projectKey = projectKey;
			details.challengeResponse = challengeResponse;

			CertificateAuthenticationResponse result = null;
			try {
				result = userAuthenticator.authenticate(details);
			} catch (AuthenticatorException e) {
				log.warn("Error while getting authenticating by certificate", e);
				throwInternalError();
			}

			if (result == null) {
				return null;
			}

			if (challengeResponse != null) {
				if (result.user == null) {
					return null;
				}

				user = (UserEntity) result.user;
			} else {
				log.debug("Returning authentication challenge for user: " + username);

				response.challenge = result.challenge;
				return response;
			}
		} else {
			return null;
		}

		if (user == null) {
			log.debug("Authentication request failed.  Username=" + username);
			return null;
		}

		log.debug("Successful authentication for user: " + user.key);

		response.access = buildAccess(user);

		return response;
	}

}
