package org.platformlayer.service.dns.client.dnsrecordlist;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.cellview.client.TextColumn;

public class JsArrayTextColumn<T extends JavaScriptObject> extends TextColumn<T> {

	final String key;

	public JsArrayTextColumn(String key) {
		super();
		this.key = key;
	}

	@Override
	public String getValue(T object) {
		JsArrayString array = JsHelpers.getArrayString(object, key);
		if (array == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length(); i++) {
			if (i != 0) {
				sb.append(",");
			}

			String s = array.get(i);
			if (s != null) {
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public static <T extends JavaScriptObject> JsTextColumn<T> build(String key) {
		return new JsTextColumn<T>(key);
	}

}
