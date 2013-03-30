package org.openstack.keystone.resources.user;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.platformlayer.auth.model.AuthenticateRequest;
import org.platformlayer.auth.model.AuthenticateResponse;
import org.platformlayer.auth.services.AsyncExecutor;
import org.platformlayer.auth.services.LoginLimits;
import org.platformlayer.auth.services.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * This is a servlet, so it can be async
 * 
 */
// @Path("/v2.0/tokens")
@Singleton
public class RestLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(RestLoginServlet.class);

	@Inject
	LoginLimits limits;

	@Inject
	Marshaller marshaller;

	@Inject
	LoginService loginService;

	@Inject
	AsyncExecutor asyncExecutor;

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

			String username = getUsername(request);

			if (Strings.isNullOrEmpty(username)) {
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			if (checkLimit && limits.isOverLimit(httpRequest, username)) {
				final AsyncContext asyncContext = httpRequest.startAsync(httpRequest, httpResponse);

				asyncExecutor.schedule(LoginService.OVER_LIMIT_DELAY, new Runnable() {
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

			AuthenticateResponse authenticateResponse = loginService.authenticate(httpRequest, request);
			if (authenticateResponse == null) {
				limits.recordFail(httpRequest, username);

				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			marshaller.write(httpRequest, httpResponse, authenticateResponse);
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

}
