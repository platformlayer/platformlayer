package org.platformlayer;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.OpenstackAuthenticationException;
import org.platformlayer.exceptions.OpenstackClientConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlatformLayerHttpTransport {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerHttpTransport.class);

	final Authenticator authClient;
	PrintStream debug = null;

	final String platformlayerEndpoint;

	public PlatformLayerHttpTransport(String platformlayerEndpoint, Authenticator authClient) {
		this.platformlayerEndpoint = platformlayerEndpoint;
		this.authClient = authClient;
	}

	private static final long SLEEP_BETWEEN_RETRIES = 200;

	public static final Integer HTTP_500_ERROR = new Integer(500);

	public AuthenticationToken getAuthenticationToken() throws PlatformLayerAuthenticationException {
		try {
			return authClient.getAuthenticationToken();
		} catch (OpenstackAuthenticationException e) {
			throw new PlatformLayerAuthenticationException("Error authenticating", e);
		}
	}

	protected <T> T doSimpleRequest(String relativePath, Class<T> retvalClass, Format format)
			throws PlatformLayerClientException {
		return doRequest("GET", relativePath, retvalClass, format, null, null);
	}

	private String getPlatformLayerEndpoint() throws PlatformLayerClientException {
		return platformlayerEndpoint;
	}

	// String url = getAuthenticationToken().getServiceUrl(DirectPlatformLayerClient.SERVICE_PLATFORMLAYER);
	// if (url == null) {
	// throw new PlatformLayerClientException("PlatformLayer endpoint not found");
	// }
	// return url;
	// }

	private URI buildUri(String relativePath) throws PlatformLayerClientException {
		String urlString = getPlatformLayerEndpoint();
		if (!urlString.endsWith("/") && !relativePath.startsWith("/")) {
			urlString += "/";
		}
		urlString += relativePath;

		URI uri;
		try {
			uri = new URI(urlString);
		} catch (URISyntaxException e) {
			throw new PlatformLayerClientException("Error building openstack URI: " + urlString, e);
		}

		String host = uri.getHost();
		if (host.equals("0.0.0.0")) {
			host = authClient.getHost();
			try {
				uri = new URI(uri.getScheme(), uri.getUserInfo(), host, uri.getPort(), uri.getPath(), uri.getQuery(),
						uri.getFragment());
			} catch (URISyntaxException e) {
				throw new IllegalStateException("Error building URI", e);
			}
		}
		return uri;
	}

	protected <T> T doRequest(String method, String relativePath, Class<T> retvalClass, Format acceptFormat,
			Object sendData, Format sendDataFormat) throws PlatformLayerClientException {
		int maxRetries = 1;
		for (int i = 1; i <= maxRetries; i++) {
			PlatformLayerHttpRequest request = new PlatformLayerHttpRequest(this, method, buildUri(relativePath));
			request.debug = debug;
			try {
				if (i == maxRetries) {
					return request.doRequest(retvalClass, acceptFormat, sendData, sendDataFormat);
				} else {
					try {
						return request.doRequest(retvalClass, acceptFormat, sendData, sendDataFormat);
					} catch (OpenstackAuthenticationException e) {
						log.warn("Reauthorizing after auth error", e);
						authClient.clearAuthenticationToken();
					} catch (OpenstackClientConnectionException e) {
						log.warn("Retrying after connection error", e);
					} catch (PlatformLayerClientException e) {
						if (HTTP_500_ERROR.equals(e.getHttpResponseCode())) {
							log.warn("Retrying after 500 error from Openstack", e);
						} else {
							log.info("Response code = " + e.getHttpResponseCode());
							throw e;
						}
					}

					try {
						Thread.sleep(SLEEP_BETWEEN_RETRIES);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			} finally {
				IoUtils.safeClose(request);
			}
		}

		throw new IllegalStateException(); // Unreachable, but the compiler doesn't know that
	}

	public PrintStream getDebug() {
		return debug;
	}

	public void setDebug(PrintStream debug) {
		this.authClient.setDebug(debug);
		this.debug = debug;
	}
}
