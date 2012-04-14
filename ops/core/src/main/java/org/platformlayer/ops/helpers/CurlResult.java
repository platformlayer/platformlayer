package org.platformlayer.ops.helpers;

import java.util.Map;

import org.platformlayer.TimeSpan;

public class CurlResult {
	static final String METADATA_HTTP_CODE = "http_code";
	static final String METADATA_TIME_TOTAL = "time_total";

	final String body;
	final Map<String, String> metadata;
	final String headers;

	public CurlResult(String body, String headers, Map<String, String> metadata) {
		this.body = body;
		this.headers = headers;
		this.metadata = metadata;
	}

	public int getHttpResult() {
		String metadataValue = getMetadataRequired(METADATA_HTTP_CODE);
		return Integer.parseInt(metadataValue);
	}

	public TimeSpan getTimeTotal() {
		String metadataValue = getMetadataRequired(METADATA_TIME_TOTAL);

		float seconds = Float.parseFloat(metadataValue);
		long milliseconds = (long) (1000.0 * seconds);
		return TimeSpan.fromMilliseconds(milliseconds);
	}

	private String getMetadataRequired(String key) {
		String value = metadata.get(key);
		if (value == null) {
			throw new IllegalStateException("Metadata value not found in curl response: " + key);
		}
		return value;
	}

	public String getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "CurlResult [metadata=" + metadata + ", headers=" + headers + ", body=" + body + "]";
	}

}
