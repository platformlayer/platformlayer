package org.platformlayer.http;

import java.io.Closeable;
import java.net.URI;

public interface HttpConfiguration extends Closeable {
	HttpRequest buildRequest(String method, URI uri);
}
