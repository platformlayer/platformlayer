package org.platformlayer.ops.templates;

import org.platformlayer.ops.OpsException;

public class TemplateException extends OpsException {
	private static final long serialVersionUID = 1L;

	public TemplateException() {
		super();
	}

	public TemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	public TemplateException(String message) {
		super(message);
	}

	public TemplateException(Throwable cause) {
		super(cause);
	}
}
