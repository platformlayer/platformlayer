package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class JobLogLine extends JavaScriptObject {
	protected JobLogLine() {
	}

	public final native String getMessage()
	/*-{ return this.message; }-*/;

	// Long is not allowed in JSON / JSNI
	// public final native long getTimestamp()
	// /*-{ return this.timestamp; }-*/;

	public final native int getLevel()
	/*-{ return this.level; }-*/;

	public final native JobLogExceptionInfo getException()
	/*-{ return this.exception; }-*/;

}
