package org.platformlayer.auth;

import org.platformlayer.http.SimpleHttpRequest;

public interface AuthenticationToken {
    String getServiceUrl(String serviceKey);

    void populateRequest(SimpleHttpRequest httpRequest);
}
