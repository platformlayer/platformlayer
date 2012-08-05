package org.platformlayer.gwt.client;

public class HttpStatusCodeException extends Exception {
	private static final long serialVersionUID = 1L;

	final int statusCode;
	final String statusText;

	public HttpStatusCodeException(int statusCode, String statusText) {
		super();
		this.statusCode = statusCode;
		this.statusText = statusText;
	}

	public static boolean is401(Throwable caught) {
		if (caught instanceof HttpStatusCodeException) {
			return ((HttpStatusCodeException) caught).statusCode == 401;
		}
		return false;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

}
