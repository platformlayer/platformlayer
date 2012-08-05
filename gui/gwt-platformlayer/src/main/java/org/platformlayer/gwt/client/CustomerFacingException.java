package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.widgets.AlertContainer.AlertLevel;

/**
 * A user-facing exception
 * 
 */
public class CustomerFacingException extends Exception {

	private static final long serialVersionUID = 1L;

	public static final int STATUS_CODE = 400;

	private final AlertLevel level;

	public CustomerFacingException(String message, Throwable cause, AlertLevel level) {
		super(message, cause);
		this.level = level;
	}

	public CustomerFacingException(String message, AlertLevel level) {
		this(message, null, level);
	}

	public CustomerFacingException(String message) {
		this(message, AlertLevel.Error);
	}

	public AlertLevel getAlertLevel() {
		return level;
	}

}
