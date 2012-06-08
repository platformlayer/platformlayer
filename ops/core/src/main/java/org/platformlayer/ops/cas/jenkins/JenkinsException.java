package org.platformlayer.ops.cas.jenkins;

public class JenkinsException extends Exception {
	private static final long serialVersionUID = 1L;
	private int httpStatusCode;

	public JenkinsException(String message, Throwable cause) {
		super(message, cause);
	}

	public JenkinsException(String message, int httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

}
