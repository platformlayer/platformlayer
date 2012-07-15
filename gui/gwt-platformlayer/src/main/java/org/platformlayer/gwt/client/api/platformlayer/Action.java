package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class Action extends JavaScriptObject {
	protected Action() {
	}

	public final native String getName()
	/*-{ return this.name; }-*/;

	public final native void setName(String name)
	/*-{ this.name = name; }-*/;
}