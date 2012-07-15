package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JobLog extends JavaScriptObject {
	protected JobLog() {
	}

	public final native JsArray<JobLogLine> getLines0()
	/*-{ return this.lines; }-*/;

	public final List<JobLogLine> getLines() {
		return JsArrayToList.wrap(getLines0());
	}
}