package org.platformlayer.service.dns.client.dnsrecordlist;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class JsHelpers {
	public static final native String getString(JavaScriptObject o, String key)
	/*-{
	var v = o[key];
	return v.toString ? v.toString() : "";
	}-*/;

	public static final native JavaScriptObject get(JavaScriptObject o, String key)
	/*-{
	return o[key];
	}-*/;

	public static final native JsArrayString getArrayString(JavaScriptObject o, String key)
	/*-{
	var v = o[key];
	return v ? v : null;
	}-*/;
}
