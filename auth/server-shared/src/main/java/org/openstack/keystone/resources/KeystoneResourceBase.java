package org.openstack.keystone.resources;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.ServiceAccount;
import org.openstack.keystone.services.SystemAuthenticator;
import org.platformlayer.TimeSpan;
import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;

public class KeystoneResourceBase {
	static final Logger log = Logger.getLogger(KeystoneResourceBase.class);

	protected static final String AUTH_HEADER = "X-Auth-Token";
	protected static final TimeSpan TOKEN_VALIDITY = new TimeSpan("1h");

	public static final String APPLICATION_JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON;
	public static final String APPLICATION_XML = javax.ws.rs.core.MediaType.APPLICATION_XML;

	// @Inject
	// protected AuthenticationFacade authentication;

	@Context
	HttpHeaders httpHeaders;

	@Context
	HttpServletRequest request;

	@Inject
	SystemAuthenticator systemAuthenticator;

	@Inject
	protected KeystoneUserAuthenticator userAuthenticator;

	protected void throw404NotFound() {
		throw new WebApplicationException(404);
	}

	protected void throwUnauthorized() {
		throw new WebApplicationException(401);
	}

	protected void throwInternalError() {
		throw new WebApplicationException(500);
	}

	protected String getAuthHeader() {
		List<String> authHeader = httpHeaders.getRequestHeader(AUTH_HEADER);
		if (authHeader == null || authHeader.isEmpty()) {
			return null;
		}
		return authHeader.get(0);
	}

	protected void requireSystemToken() throws AuthenticatorException {
		X509Certificate[] certChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
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

	protected boolean isNullOrEmpty(List<?> list) {
		return (list == null) || (list.isEmpty());
	}

}
