package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class Token extends JavaScriptObject {
	protected Token() {
	}

	public final native String getId()
	/*-{ return this.id; }-*/;

	public final native String getExpires()
	/*-{ return this.expires; }-*/;
}