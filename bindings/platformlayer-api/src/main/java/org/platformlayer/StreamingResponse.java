package org.platformlayer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.platformlayer.http.HttpResponse;

import com.google.common.io.Closeables;

public class StreamingResponse implements Closeable {

	private final InputStream responseStream;
	private final HttpResponse response;

	public StreamingResponse(HttpResponse response) throws IOException {
		this.response = response;
		this.responseStream = response.getInputStream();
	}

	@Override
	public void close() throws IOException {
		Closeables.closeQuietly(responseStream);
		Closeables.closeQuietly(response);
	}

	public InputStream getResponseStream() {
		return responseStream;
	}
}
