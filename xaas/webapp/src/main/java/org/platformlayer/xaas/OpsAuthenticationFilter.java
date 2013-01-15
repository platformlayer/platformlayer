package org.platformlayer.xaas;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.*;
import org.platformlayer.Scope;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.PlatformlayerAuthenticationToken;
import org.platformlayer.model.AuthenticationCredentials;
import org.platformlayer.ops.DirectAuthentication;

public class OpsAuthenticationFilter implements Filter {
	static final Logger log = LoggerFactory.getLogger(OpsAuthenticationFilter.class);

	// private static final long MAX_TIMESTAMP_SKEW = 300L * 1000L;

	@Inject
	AuthenticationTokenValidator authenticationTokenValidator;

	// protected void populateScope(Scope authenticatedScope, Authentication auth) throws Exception {
	// authenticatedScope.put(Authentication.class, auth);
	//
	// OpsProject project;
	// OpsUser user = null;
	// if (auth instanceof DirectAuthentication) {
	// project = ((DirectAuthentication) auth).getOpsProject();
	// if (project == null) {
	// throw new IllegalStateException();
	// }
	// } else {
	// KeystoneUser keystoneUser = new KeystoneUser((KeystoneUserAuthentication) auth);
	// user = keystoneUser;
	//
	// // String projectKey = auth.getProject().getName();
	// // project = authenticationService.findProject(user, projectKey);
	// //
	// // if (project == null) {
	// // log.warn("Project not found: " + projectKey);
	// // throw new SecurityException();
	// // }
	// }
	//
	// OpsAuthentication opsAuthentication = new OpsAuthentication(auth, user, project);
	//
	// authenticatedScope.put(OpsAuthentication.class, opsAuthentication);
	// }

	protected AuthenticationCredentials findCredentials(HttpServletRequest httpRequest) throws Exception {
		AuthenticationCredentials creds = null;

		final String authToken = httpRequest.getHeader("X-Auth-Token");
		if (authToken != null) {
			creds = new AuthenticationCredentials() {
				@Override
				public AuthenticationToken getToken() {
					return new PlatformlayerAuthenticationToken(authToken);
				}
			};
		}

		if (creds == null) {
			// Direct authentication
			// TODO: Enforce SSL?
			String authKey = httpRequest.getHeader("X-Auth-Key");
			String authSecret = httpRequest.getHeader("X-Auth-Secret");

			if (authKey != null && authSecret != null) {
				creds = DirectAuthentication.build(authKey, authSecret);
			}
		}

		return creds;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		Scope authenticatedScope = Scope.inherit();

		// Fail safe
		authenticatedScope.put(AuthenticationCredentials.class, null);

		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

			try {
				AuthenticationCredentials credentials = findCredentials(httpServletRequest);

				// if (authenticated == null) {
				// httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				// return;
				// } else {
				// populateScope(authenticatedScope, authenticated);
				// }

				authenticatedScope.put(AuthenticationCredentials.class, credentials);
			} catch (SecurityException e) {
				httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			} catch (Exception e) {
				// If we're down, don't tell the user that their password is wrong
				log.warn("Unexpected error in authentication filter", e);
				httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		}

		authenticatedScope.push();
		try {
			filterChain.doFilter(servletRequest, servletResponse);
		} finally {
			authenticatedScope.pop();
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

}
