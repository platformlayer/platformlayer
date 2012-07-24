package org.platformlayer.gwt.client.api.platformlayer;

import com.google.gwt.core.client.JavaScriptObject;

public class Job extends JavaScriptObject {
	protected Job() {
	}

	// TODO: JSON looks like: key : { value: abc } Remove the value!
	public final native String getKey0()
	/*-{ return this.key ? this.key.value : null; }-*/;

	// TODO: JSON looks like: key : { value: abc } Remove the value!
	public final native String getTargetId()
	/*-{ return this.targetId ? this.targetId.value : null; }-*/;

	public final native Action getAction()
	/*-{ return this.action; }-*/;

	public final native String getState0()
	/*-{ return this.state; }-*/;

	public final native JobLog getLog()
	/*-{ return this.log; }-*/;

	public final String getJobId() {
		String key = getKey0();
		assert key != null;

		int lastSlash = key.lastIndexOf('/');
		assert lastSlash != -1;

		return key.substring(lastSlash + 1);
	}

	public final JobState getState() {
		String state = getState0();
		if (state == null) {
			return null;
		}
		return JobState.valueOf(state);
	}

}