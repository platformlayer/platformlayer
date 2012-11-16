package org.platformlayer.auth.resources;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.platformlayer.TimeSpan;
import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;
import org.platformlayer.auth.services.RegistrationService;
import org.platformlayer.web.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlatformlayerAuthResourceBase {
	private static final Logger log = LoggerFactory.getLogger(PlatformlayerAuthResourceBase.class);

	protected static final String AUTH_HEADER = "X-Auth-Token";
	protected static final TimeSpan TOKEN_VALIDITY = new TimeSpan("1h");

	public static final String JSONP = "application/javascript";
	public static final String APPLICATION_JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON;
	public static final String APPLICATION_XML = javax.ws.rs.core.MediaType.APPLICATION_XML;

	// @Inject
	// protected AuthenticationFacade authentication;

	@Context
	HttpHeaders httpHeaders;

	@Context
	protected HttpServletRequest request;

	@Inject
	protected KeystoneUserAuthenticator userAuthenticator;

	@Inject
	protected RegistrationService registrationService;

	protected void throw404NotFound() {
		throw new WebApplicationException(404);
	}

	protected void throwUnauthorized() {
		throw new WebApplicationException(401);
	}

	protected void throwInternalError() {
		throw new WebApplicationException(500);
	}

	protected boolean isNullOrEmpty(List<?> list) {
		return (list == null) || (list.isEmpty());
	}

	protected X509Certificate[] getCertificateChain() {
		return HttpUtils.getCertificateChain(request);
	}

}
