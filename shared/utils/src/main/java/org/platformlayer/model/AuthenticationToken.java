package org.platformlayer.model;

import org.platformlayer.http.HttpRequest;

public interface AuthenticationToken {
	void populateRequest(HttpRequest httpRequest);
}
