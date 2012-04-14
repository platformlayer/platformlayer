package org.openstack.keystone.resources;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.apache.log4j.Logger;
import org.openstack.keystone.model.Auth;
import org.openstack.keystone.services.AuthenticationFacade;
import org.openstack.keystone.services.AuthenticationInfo;
import org.openstack.keystone.services.AuthenticatorException;
import org.openstack.keystone.services.TokenInfo;
import org.platformlayer.TimeSpan;

public class KeystoneResourceBase {
	static final Logger log = Logger.getLogger(KeystoneResourceBase.class);

	protected static final String AUTH_HEADER = "X-Auth-Token";
	protected static final TimeSpan TOKEN_VALIDITY = new TimeSpan("1h");

	public static final String APPLICATION_JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON;
	public static final String APPLICATION_XML = javax.ws.rs.core.MediaType.APPLICATION_XML;

	@Inject
	protected AuthenticationFacade authentication;

	@Context
	HttpHeaders httpHeaders;

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

	protected TokenInfo requireSystemToken() {
		String authHeader = getAuthHeader();

		TokenInfo myTokenInfo = authentication.validateToken(true, authHeader);
		if (myTokenInfo == null || !myTokenInfo.isSystem()) {
			throwUnauthorized();
		}

		return myTokenInfo;
	}

	protected boolean isNullOrEmpty(List<?> list) {
		return (list == null) || (list.isEmpty());
	}

	protected TokenInfo tryAuthenticate(boolean isSystem, Auth request) throws AuthenticatorException {
		String username = null;
		String password = null;
		String scope = request.tenantName;

		if (request.passwordCredentials != null) {
			username = request.passwordCredentials.username;
			password = request.passwordCredentials.password;
		}

		AuthenticationInfo authenticated = authentication.authenticate(isSystem, username, password);
		if (authenticated == null) {
			log.debug("Authentication request failed for " + username);

			return null;
		}

		return buildToken(isSystem, scope, authenticated.getUserId(), authenticated.getTokenSecret());
	}

	private TokenInfo buildToken(boolean isSystem, String scope, String userId, byte[] tokenSecret) {
		Date now = new Date();
		Date expiration = TOKEN_VALIDITY.addTo(now);

		byte flags = 0;
		if (isSystem) {
			flags |= TokenInfo.FLAG_SYSTEM;
		}
		TokenInfo tokenInfo = new TokenInfo(flags, scope, userId, expiration, tokenSecret);

		return tokenInfo;
	}
}
