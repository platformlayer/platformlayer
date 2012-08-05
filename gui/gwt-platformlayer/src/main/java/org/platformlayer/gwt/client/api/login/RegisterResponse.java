package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class RegisterResponse extends JavaScriptObject {
	protected RegisterResponse() {
	}

	public final native Access getAccess()
	/*-{ return this.access; }-*/;

	public final native String getErrorMessage()
	/*-{ return this.errorMessage; }-*/;

	// public final native int getStatusCode()
	// /*-{ return this.statusCode || 0; }-*/;
}