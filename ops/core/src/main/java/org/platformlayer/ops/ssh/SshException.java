package org.platformlayer.ops.ssh;

/**
 * Generic exception thrown by our SSH utilties.
 * 
 * @author justinsb
 * 
 */
public class SshException extends Exception {
	private static final long serialVersionUID = 1L;

	public SshException() {
		super();
	}

	public SshException(String message, Throwable cause) {
		super(message, cause);
	}

	public SshException(String message) {
		super(message);
	}

	public SshException(Throwable cause) {
		super(cause);
	}

}
