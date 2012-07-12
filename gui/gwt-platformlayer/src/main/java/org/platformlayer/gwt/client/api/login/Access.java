package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class Access extends JavaScriptObject {
	protected Access() {
	}

	public final native Token getToken()
	/*-{ return this.token; }-*/;

}