package org.platformlayer.gwt.client.api.login;

import com.google.gwt.core.client.JavaScriptObject;

public class Auth extends JavaScriptObject {
	protected Auth() {
	}

	public final native PasswordCredentials getPasswordCredentials()
	/*-{ return this.passwordCredentials; }-*/;

	public final native void setPasswordCredentials(PasswordCredentials passwordCredentials)
	/*-{ this.passwordCredentials = passwordCredentials; }-*/;
}
