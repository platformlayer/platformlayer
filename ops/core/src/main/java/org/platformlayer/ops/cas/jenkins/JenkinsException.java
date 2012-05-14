package org.platformlayer.ops.cas.jenkins;

public class JenkinsException extends Exception {
	private static final long serialVersionUID = 1L;

	public JenkinsException(String message, Throwable cause) {
		super(message, cause);
	}

	public JenkinsException(String message) {
		super(message);
	}

}
