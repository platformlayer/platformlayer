package org.platformlayer.auth.services;

import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.platformlayer.auth.AuthenticatorException;
import org.platformlayer.auth.CertificateAuthenticationRequest;
import org.platformlayer.auth.CertificateAuthenticationResponse;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;
import org.platformlayer.auth.model.AuthenticateRequest;
import org.platformlayer.auth.model.AuthenticateResponse;
import org.platformlayer.web.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.TimeSpan;

public class LoginService {
	private static final Logger log = LoggerFactory.getLogger(LoginService.class);

	@Inject
	TokenHelpers tokenHelpers;

	@Inject
	KeystoneUserAuthenticator userAuthenticator;

	public static final TimeSpan OVER_LIMIT_DELAY = TimeSpan.fromMilliseconds(2000);

	public AuthenticateResponse authenticate(HttpServletRequest httpRequest, AuthenticateRequest request) {
		AuthenticateResponse response = new AuthenticateResponse();

		String username = null;

		UserEntity user = null;

		if (request.auth.passwordCredentials != null) {
			username = request.auth.passwordCredentials.username;
			String password = request.auth.passwordCredentials.password;

			try {
				user = userAuthenticator.authenticate(username, password);
			} catch (AuthenticatorException e) {
				// An exception indicates something went wrong (i.e. not just
				// bad credentials)
				log.warn("Error while getting user info", e);
				throw new IllegalStateException("Error while getting user info", e);
			}
		} else if (request.auth.certificateCredentials != null) {
			username = request.auth.certificateCredentials.username;

			X509Certificate[] certificateChain = HttpUtils.getCertificateChain(httpRequest);
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
				log.warn("Error while authenticating by certificate", e);
				throw new IllegalStateException("Error while authenticating by certificate", e);
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

		response.access = tokenHelpers.buildAccess(user);

		return response;
	}
}
