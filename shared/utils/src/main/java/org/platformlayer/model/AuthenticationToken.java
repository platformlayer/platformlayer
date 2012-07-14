package org.platformlayer.model;

import org.platformlayer.http.SimpleHttpRequest;

public interface AuthenticationToken {
	void populateRequest(SimpleHttpRequest httpRequest);
}
