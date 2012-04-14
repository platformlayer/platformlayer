package org.openstack.keystone.service;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.platformlayer.Scope;
import org.platformlayer.model.Authentication;

public abstract class OpenstackAuthenticationFilterBase implements Filter {
	static final Logger log = Logger.getLogger(OpenstackAuthenticationFilterBase.class);

	private final AuthenticationTokenValidator authenticationTokenValidator;

	protected OpenstackAuthenticationFilterBase(AuthenticationTokenValidator authenticationTokenValidator) {
		this.authenticationTokenValidator = authenticationTokenValidator;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		Scope authenticatedScope = Scope.inherit();

		// Fail safe
		authenticatedScope.put(Authentication.class, null);

		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

			try {
				Authentication authenticated = attemptAuthentication(httpServletRequest);

				if (authenticated == null) {
					httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				} else {
					populateScope(authenticatedScope, authenticated);
				}
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

	protected Authentication attemptAuthentication(HttpServletRequest httpServletRequest) throws Exception {
		String authToken = httpServletRequest.getHeader("X-Auth-Token");

		Authentication authenticated = null;

		if (authToken != null) {
			authenticated = authenticationTokenValidator.validate(authToken);
		}

		return authenticated;
	}

	protected void populateScope(Scope authenticatedScope, Authentication authenticated) throws Exception {
		authenticatedScope.put(Authentication.class, authenticated);
	}

	@Override
	public void destroy() {

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

}
