package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class AuthenticateResponse extends JavaScriptObject {
	protected AuthenticateResponse() {
	}

	public final native Access getAccess()
	/*-{ return this.access; }-*/;

	public final native int getStatusCode()
	/*-{ return  this.statusCode || 0; }-*/;
}