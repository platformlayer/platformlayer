package org.platformlayer.rest;

import java.io.PrintStream;
import java.net.URI;

public interface RestfulClient {
	<T> RestfulRequest<T> buildRequest(String method, String relativeUri, Object postObject, Class<T> responseClass);

	URI getBaseUri();

	void setDebug(PrintStream debug);
}
