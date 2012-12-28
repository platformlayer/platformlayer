package org.platformlayer;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.auth.Authenticator;
import org.platformlayer.auth.PlatformlayerAuthenticationClientException;
import org.platformlayer.exceptions.OpenstackClientConnectionException;
import org.platformlayer.http.HttpStrategy;
import org.platformlayer.ids.ProjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;

class PlatformLayerHttpTransport {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerHttpTransport.class);

	final Authenticator authClient;
	PrintStream debug = null;

	final String platformlayerEndpoint;

	final List<String> trustKeys;

	final HttpStrategy httpStrategy;

	public PlatformLayerHttpTransport(HttpStrategy httpStrategy, String platformlayerEndpoint,
			Authenticator authClient, List<String> trustKeys) {
		this.httpStrategy = httpStrategy;
		this.platformlayerEndpoint = platformlayerEndpoint;
		this.authClient = authClient;
		this.trustKeys = trustKeys;
	}

	private static final long SLEEP_BETWEEN_RETRIES = 200;

	public static final Integer HTTP_500_ERROR = new Integer(500);

	public AuthenticationToken getAuthenticationToken() throws PlatformLayerAuthenticationException {
		try {
			return authClient.getAuthenticationToken();
		} catch (PlatformlayerAuthenticationClientException e) {
			throw new PlatformLayerAuthenticationException("Error authenticating", e);
		}
	}

	// private String getPlatformLayerEndpoint() throws PlatformLayerClientException {
	// return platformlayerEndpoint;
	// }

	// String url = getAuthenticationToken().getServiceUrl(DirectPlatformLayerClient.SERVICE_PLATFORMLAYER);
	// if (url == null) {
	// throw new PlatformLayerClientException("PlatformLayer endpoint not found");
	// }
	// return url;
	// }

	private URI buildUri(String relativePath) throws PlatformLayerClientException {
		if (relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1);
		}

		String urlString = platformlayerEndpoint;
		if (!urlString.endsWith("/")) {
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
			PlatformLayerHttpRequest request = new PlatformLayerHttpRequest(this, method, buildUri(relativePath),
					trustKeys);
			request.debug = debug;
			try {
				T retval = null;
				if (i == maxRetries) {
					retval = request.doRequest(retvalClass, acceptFormat, sendData, sendDataFormat);
				} else {
					try {
						retval = request.doRequest(retvalClass, acceptFormat, sendData, sendDataFormat);
					} catch (PlatformLayerAuthenticationException e) {
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

				if (retval != null) {
					if (retval instanceof StreamingResponse) {
						// Don't close
						request = null;
					}
					return retval;
				}
			} finally {
				Closeables.closeQuietly(request);
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

	public PlatformLayerEndpointInfo getEndpointInfo(ProjectId projectId) {
		return new PlatformLayerEndpointInfo(authClient, platformlayerEndpoint, projectId, trustKeys);
	}

	public HttpStrategy getHttpStrategy() {
		return httpStrategy;
	}
}
