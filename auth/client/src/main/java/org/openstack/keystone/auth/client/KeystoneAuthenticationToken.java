package org.openstack.keystone.auth.client;

import org.openstack.docs.identity.api.v2.Access;
import org.platformlayer.auth.AuthenticationToken;
import org.platformlayer.http.SimpleHttpRequest;

public class KeystoneAuthenticationToken implements AuthenticationToken {
	private final Access access;

	public KeystoneAuthenticationToken(Access access) {
		this.access = access;
	}

	public String getAuthTokenValue() {
		return access.getToken().getId();
	}

	@Override
	public String getServiceUrl(String serviceKey) {
		// for (Service service : access.getServiceCatalog()) {
		// if (Objects.equal(service.getType(), serviceKey)) {
		// String bestUrl = null;
		// for (ServiceEndpoint endpoint : service.getEndpoints()) {
		// bestUrl = endpoint.getPublicURL();
		// if (bestUrl != null) {
		// break;
		// }
		// }
		//
		// if (bestUrl != null) {
		// return bestUrl;
		// }
		// }
		// }
		return null;
	}

	@Override
	public void populateRequest(SimpleHttpRequest httpRequest) {
		httpRequest.setRequestHeader("X-Auth-Token", getAuthTokenValue());
	}

}