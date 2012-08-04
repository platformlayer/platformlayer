package org.platformlayer;

/**
 * An exception where it is OK to present getMessage to the end-user
 * 
 * @author justinsb
 * 
 */
public class CustomerFacingException extends Exception {
	private static final long serialVersionUID = 1L;

	public CustomerFacingException(String message, Throwable cause) {
		super(message, cause);
	}

	public CustomerFacingException(String message) {
		super(message);
	}

}
