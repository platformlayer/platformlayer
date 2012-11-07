package org.platformlayer.service.dns.client.dnsrecordlist;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.cellview.client.TextColumn;

public class JsTextColumn<T extends JavaScriptObject> extends TextColumn<T> {

	final String key;

	public JsTextColumn(String key) {
		super();
		this.key = key;
	}

	@Override
	public String getValue(T object) {
		return JsHelpers.getString(object, key);
	}

	public static <T extends JavaScriptObject> JsTextColumn<T> build(String key) {
		return new JsTextColumn<T>(key);
	}

}
