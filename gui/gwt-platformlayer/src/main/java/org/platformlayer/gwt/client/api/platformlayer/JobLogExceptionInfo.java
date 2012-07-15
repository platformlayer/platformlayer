package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsStringArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JobLogExceptionInfo extends JavaScriptObject {
	protected JobLogExceptionInfo() {
	}

	public final native JsArrayString getInfo0()
	/*-{ return this.info; }-*/;

	public final List<String> getInfo() {
		return JsStringArrayToList.wrap(getInfo0());
	}
}
