package org.openstack.keystone.resources.user;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

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
import com.google.common.base.Strings;

/**
 * This is a servlet, so it can be async
 * 
 */
// @Path("/v2.0/tokens")
@Singleton
public class TokensServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(TokensServlet.class);

	@Inject
	LoginLimits limits;

	@Inject
	Marshaller marshaller;

	@Inject
	TokenHelpers tokenHelpers;

	@Inject
	KeystoneUserAuthenticator userAuthenticator;

	@Inject
	AsyncExecutor asyncExecutor;

	static final TimeSpan OVER_LIMIT_DELAY = TimeSpan.fromMilliseconds(2000);

	@Override
	protected void doPost(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
			throws ServletException, IOException {
		AuthenticateRequest request = marshaller.read(httpRequest, AuthenticateRequest.class);

		if (request == null) {
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		processRequest(httpRequest, httpResponse, request, true);
	}

	protected void processRequest(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
			final AuthenticateRequest request, boolean checkLimit) throws IOException {
		try {
			if (request.auth == null) {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			String ip = httpRequest.getRemoteAddr();
			String username = getUsername(request);

			if (Strings.isNullOrEmpty(username)) {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			if (checkLimit && limits.isOverLimit(ip, username)) {
				final AsyncContext asyncContext = httpRequest.startAsync(httpRequest, httpResponse);

				asyncExecutor.schedule(OVER_LIMIT_DELAY, new Runnable() {
					@Override
					public void run() {
						try {
							processRequest(httpRequest, httpResponse, request, false);
							asyncContext.complete();
						} catch (Exception e) {
							log.error("Unexpected error caught in async task", e);
						}
					}
				});
				return;
			}

			AuthenticateResponse response = authenticate(httpRequest, request);
			if (response == null) {
				limits.recordFail(ip, username);

				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			marshaller.write(httpRequest, httpResponse, response);
		} catch (WebApplicationException e) {
			log.info("Returning exception from servlet", e);
			httpResponse.sendError(e.getResponse().getStatus());
		} catch (Exception e) {
			log.warn("Unexpected error in servlet", e);
			httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private String getUsername(AuthenticateRequest request) {
		String username = null;

		if (request.auth.passwordCredentials != null) {
			username = request.auth.passwordCredentials.username;
		} else if (request.auth.certificateCredentials != null) {
			username = request.auth.certificateCredentials.username;
		} else {
			username = null;
		}

		return username;
	}

	private AuthenticateResponse authenticate(HttpServletRequest httpRequest, AuthenticateRequest request) {
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
