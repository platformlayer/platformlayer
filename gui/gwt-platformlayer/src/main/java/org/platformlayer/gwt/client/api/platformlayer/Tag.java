package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class Tag extends JavaScriptObject {
	protected Tag() {
	}

	public final native String getKey()
	/*-{ return this.key; }-*/;

	public final native String getValue()
	/*-{ return this.value; }-*/;
}
