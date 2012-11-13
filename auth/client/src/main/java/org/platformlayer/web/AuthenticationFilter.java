package org.platformlayer.web;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.platformlayer.Scope;
import org.platformlayer.auth.AuthenticationTokenValidator;
import org.platformlayer.auth.client.PlatformlayerAuthenticationToken;
import org.platformlayer.model.AuthenticationCredentials;
import org.platformlayer.model.AuthenticationToken;
import org.platformlayer.model.ProjectAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

@Singleton
public class AuthenticationFilter implements Filter {
	private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Inject
	AuthenticationTokenValidator authenticationTokenValidator;

	protected AuthenticationCredentials findCredentials(HttpServletRequest httpRequest) throws Exception {
		final String authToken = httpRequest.getHeader("X-Auth-Token");
		if (authToken != null) {
			AuthenticationCredentials creds = new AuthenticationCredentials() {
				@Override
				public AuthenticationToken getToken() {
					return new PlatformlayerAuthenticationToken(authToken);
				}
			};
			return creds;
		}

		X509Certificate[] certChain = (X509Certificate[]) httpRequest
				.getAttribute("javax.servlet.request.X509Certificate");
		if (certChain != null && certChain.length != 0) {
			AuthenticationCredentials creds = new CertificateAuthenticationCredentials(certChain);
			return creds;
		}

		return null;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		Scope authenticatedScope = Scope.empty();

		// Fail safe
		authenticatedScope.put(AuthenticationCredentials.class, null);

		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

			try {
				AuthenticationCredentials credentials = findCredentials(httpServletRequest);

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

	public static ProjectAuthorization authorizeProject(AuthenticationCredentials authn,
			AuthenticationTokenValidator authTokenValidator, String projectKey) {
		if (authn == null) {
			return null;
		}

		ProjectAuthorization authz = null;
		if (authn instanceof ProjectAuthorization) {
			authz = (ProjectAuthorization) authn;
		} else if (authn instanceof CertificateAuthenticationCredentials) {
			authz = authTokenValidator.validateChain(((CertificateAuthenticationCredentials) authn).getCertChain(),
					projectKey);
		} else {
			authz = authTokenValidator.validateToken(authn.getToken(), projectKey);
		}

		if (authz == null || !Objects.equal(authz.getName(), projectKey)) {
			return null;
		}

		return authz;
	}
}
