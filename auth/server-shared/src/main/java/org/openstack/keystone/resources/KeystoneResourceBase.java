package org.openstack.keystone.resources;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import org.platformlayer.TimeSpan;
import org.platformlayer.auth.keystone.KeystoneUserAuthenticator;

public class KeystoneResourceBase {
	static final Logger log = Logger.getLogger(KeystoneResourceBase.class);

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
		X509Certificate[] certChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		if (certChain == null || certChain.length == 0) {
			return null;
		}
		return certChain;
	}

}
