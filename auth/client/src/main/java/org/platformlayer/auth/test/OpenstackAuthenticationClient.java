package org.platformlayer.auth.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Random;

import org.platformlayer.auth.OpenstackAuthenticationException;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackAuthenticationClient {
	static final Logger log = LoggerFactory.getLogger(OpenstackAuthenticationClient.class);

	final String username;
	final private String secret;

	OpenstackAuthenticationToken authenticationToken;

	private String authenticationUrl = URL_AUTHENTICATE;

	static final String URL_AUTHENTICATE = "https://auth.api.rackspacecloud.com/v1.0";

	public static final Integer HTTP_500_ERROR = new Integer(500);

	protected static final int MAX_RETRIES = 10;

	static Random random = new Random();

	public OpenstackAuthenticationClient(String username, String secret) {
		this.username = username;
		this.secret = secret;
	}

	public synchronized OpenstackAuthenticationToken getAuthenticationToken() throws OpenstackAuthenticationException {
		if (authenticationToken == null) {
			authenticationToken = authenticate();
		}

		return authenticationToken;
	}

	private OpenstackAuthenticationToken authenticate() throws OpenstackAuthenticationException {
		if (username == null || secret == null) {
			throw new OpenstackAuthenticationException("Username and secret are both required");
		}

		try {
			// GET /v1.0 HTTP/1.1
			// Host: auth.api.rackspacecloud.com
			// X-Auth-User: jdoe
			// X-Auth-Key: a86850deb2742ec3cb41518e26aa2d89
			URI uri = new URI(authenticationUrl);

			SimpleHttpRequest httpRequest = SimpleHttpRequest.build("GET", uri);
			httpRequest.setRequestHeader("X-Auth-User", this.username);
			httpRequest.setRequestHeader("X-Auth-Key", this.secret);

			SimpleHttpResponse response = httpRequest.doRequest();

			int responseCode = response.getHttpResponseCode();
			switch (responseCode) {
			case 401:
				throw new OpenstackAuthenticationException("Openstack credentials were not correct");

			case 204:
				/*
				 * If authentication is successful, an HTTP status 204 No Content is returned with three cloud service
				 * headers, X-Server-Management-Url, X-Storage-Url, X-CDN-Management-Url, as well as X-Auth-Token
				 */

				String authToken = getRequiredHeader(response, "X-Auth-Token");

				Map<String, String> allHeaders = response.getHeadersRemoveDuplicates();

				return new OpenstackAuthenticationToken(authToken, allHeaders);

			default:
				throw new OpenstackAuthenticationException("Unexpected return code from Rackspace Cloud during login: "
						+ responseCode);
			}
		} catch (IOException e) {
			throw new OpenstackAuthenticationException("Error communicating with authentication service", e);
		} catch (URISyntaxException e) {
			throw new OpenstackAuthenticationException("Error building rackspace URI", e);
		}
	}

	private static String getRequiredHeader(SimpleHttpResponse response, String headerName)
			throws OpenstackAuthenticationException {
		String headerValue = response.getResponseHeaderField(headerName);
		if (headerValue == null) {
			throw new OpenstackAuthenticationException("Did not find required header: " + headerName);
		}
		return headerValue;
	}

	public static <T> T deserializeXml(InputStream is, Class<T> clazz) throws OpenstackAuthenticationException {
		try {
			return JaxbHelper.deserializeXmlObject(is, clazz, true);
		} catch (UnmarshalException e) {
			throw new OpenstackAuthenticationException("Error reading authentication response data", e);
		}
	}

	public String getAuthenticationUrl() {
		return authenticationUrl;
	}

	public void setAuthenticationUrl(String authenticationUrl) {
		this.authenticationUrl = authenticationUrl;
	}

	public OpenstackAuthenticationToken reauthenticate() throws OpenstackAuthenticationException {
		this.authenticationToken = null;
		return getAuthenticationToken();
	}
}
