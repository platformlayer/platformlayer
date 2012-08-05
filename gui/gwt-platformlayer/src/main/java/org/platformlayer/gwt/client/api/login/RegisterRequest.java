package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class RegisterRequest extends JavaScriptObject {
	protected RegisterRequest() {
	}

	public final native String getUsername()
	/*-{ return this.username; }-*/;

	public final native void setUsername(String username)
	/*-{ this.username = username; }-*/;

	public final native String getPassword()
	/*-{ return this.password; }-*/;

	public final native void setPassword(String password)
	/*-{ this.password = password; }-*/;
}
