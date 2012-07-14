package org.platformlayer.rest;

public class RestClientException extends Exception {
	private static final long serialVersionUID = 1L;
	private final Integer httpResponseCode;

	public RestClientException(String message, Throwable cause, Integer httpResponseCode) {
		super(message, cause);
		this.httpResponseCode = httpResponseCode;
	}

	public RestClientException(String message, Throwable cause) {
		super(message, cause);
		this.httpResponseCode = null;
	}

	public RestClientException(String message) {
		super(message);
		this.httpResponseCode = null;
	}

	public Integer getHttpResponseCode() {
		return httpResponseCode;
	}

}
