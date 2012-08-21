package org.platformlayer.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpResponse extends Closeable {

	InputStream getInputStream() throws IOException;

	int getHttpResponseCode() throws IOException;

	InputStream getErrorStream() throws IOException;

	String getResponseHeaderField(String string);

	Map<String, List<String>> getHeaderFields();

}
