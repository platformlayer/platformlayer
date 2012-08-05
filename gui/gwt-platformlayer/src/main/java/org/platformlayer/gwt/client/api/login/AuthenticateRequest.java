package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class AuthenticateRequest extends JavaScriptObject {
	protected AuthenticateRequest() {
	}

	public final native Auth getAuth()
	/*-{ return this.auth; }-*/;

	public final native void setAuth(Auth auth)
	/*-{ this.auth = auth; }-*/;
}