package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class Job extends JavaScriptObject {
	protected Job() {
	}

	// TODO: JSON looks like: key : { value: abc } Remove the value!
	public final native String getKey()
	/*-{ return this.key ? this.key.value : null; }-*/;

	// TODO: JSON looks like: key : { value: abc } Remove the value!
	public final native String getTargetId()
	/*-{ return this.targetId ? this.targetId.value : null; }-*/;

	public final native Action getAction()
	/*-{ return this.action; }-*/;

	public final native String getState()
	/*-{ return this.state; }-*/;

}