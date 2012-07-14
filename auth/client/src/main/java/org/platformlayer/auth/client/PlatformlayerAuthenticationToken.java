package org.platformlayer.auth.client;

import org.platformlayer.auth.v1.Access;
import org.platformlayer.http.SimpleHttpRequest;
import org.platformlayer.model.AuthenticationToken;

public class PlatformlayerAuthenticationToken implements AuthenticationToken {
	private final String authToken;

	public PlatformlayerAuthenticationToken(String authToken) {
		this.authToken = authToken;
	}

	public PlatformlayerAuthenticationToken(Access access) {
		this.authToken = access.getToken().getId();
	}

	public String getAuthTokenValue() {
		return authToken;
	}

	// @Override
	// public String getServiceUrl(String serviceKey) {
	// // for (Service service : access.getServiceCatalog()) {
	// // if (Objects.equal(service.getType(), serviceKey)) {
	// // String bestUrl = null;
	// // for (ServiceEndpoint endpoint : service.getEndpoints()) {
	// // bestUrl = endpoint.getPublicURL();
	// // if (bestUrl != null) {
	// // break;
	// // }
	// // }
	// //
	// // if (bestUrl != null) {
	// // return bestUrl;
	// // }
	// // }
	// // }
	// return null;
	// }

	@Override
	public void populateRequest(SimpleHttpRequest httpRequest) {
		httpRequest.setRequestHeader("X-Auth-Token", getAuthTokenValue());
	}

}