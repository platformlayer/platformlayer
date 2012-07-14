package org.platformlayer.gwt.client.api.login;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsStringArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class Access extends JavaScriptObject {
	protected Access() {
	}

	public final native Token getToken()
	/*-{ return this.token; }-*/;

	public final native JsArrayString getProjects0()
	/*-{ return this.projects; }-*/;

	public final List<String> getProjects() {
		return JsStringArrayToList.wrap(getProjects0());
	}
}