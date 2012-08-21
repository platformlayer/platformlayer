package org.platformlayer.http;

import java.net.URI;

public interface HttpConfiguration {
	HttpRequest buildRequest(String method, URI uri);
}
