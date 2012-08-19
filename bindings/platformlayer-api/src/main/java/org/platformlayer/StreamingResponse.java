package org.platformlayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.platformlayer.http.SimpleHttpRequest.SimpleHttpResponse;

public class StreamingResponse implements Closeable {

	private final InputStream responseStream;
	private final SimpleHttpResponse response;

	public StreamingResponse(SimpleHttpResponse response) throws IOException {
		this.response = response;
		this.responseStream = response.getInputStream();
	}

	@Override
	public void close() throws IOException {
		IoUtils.safeClose(responseStream);
		IoUtils.safeClose(response);
	}

	public InputStream getResponseStream() {
		return responseStream;
	}
}
