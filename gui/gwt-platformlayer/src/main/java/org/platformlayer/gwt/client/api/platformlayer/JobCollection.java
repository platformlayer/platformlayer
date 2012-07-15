package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JobCollection extends JavaScriptObject {
	protected JobCollection() {
	}

	public final native JsArray<Job> getJobs0()
	/*-{ return this.jobs; }-*/;

	public final List<Job> getJobs() {
		return JsArrayToList.wrap(getJobs0());
	}
}
