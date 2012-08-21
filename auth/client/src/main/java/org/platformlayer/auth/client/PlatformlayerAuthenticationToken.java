package org.platformlayer.auth.client;

import org.platformlayer.auth.v1.Access;
import org.platformlayer.http.HttpRequest;
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
	public void populateRequest(HttpRequest httpRequest) {
		httpRequest.setRequestHeader("X-Auth-Token", getAuthTokenValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authToken == null) ? 0 : authToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PlatformlayerAuthenticationToken other = (PlatformlayerAuthenticationToken) obj;
		if (authToken == null) {
			if (other.authToken != null) {
				return false;
			}
		} else if (!authToken.equals(other.authToken)) {
			return false;
		}
		return true;
	}

}