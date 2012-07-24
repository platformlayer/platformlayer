package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class JobLogLine extends JavaScriptObject {
	protected JobLogLine() {
	}

	public final static int LEVEL_FATAL = 50000;
	public final static int LEVEL_ERROR = 40000;
	public final static int LEVEL_WARN = 30000;
	public final static int LEVEL_INFO = 20000;
	public final static int LEVEL_DEBUG = 10000;

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
