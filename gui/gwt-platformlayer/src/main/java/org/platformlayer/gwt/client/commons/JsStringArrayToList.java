package org.platformlayer.gwt.client.commons;

import com.google.gwt.core.client.JsArrayString;

public class JsStringArrayToList extends JsArrayToListBase<String> {

	final JsArrayString array;

	public JsStringArrayToList(JsArrayString array) {
		super();
		this.array = array;
	}

	@Override
	public int size() {
		return array.length();
	}

	@Override
	public String get(int index) {
		return array.get(index);
	}

	public static JsStringArrayToList build(JsArrayString array) {
		return new JsStringArrayToList(array);
	}

	public static JsStringArrayToList wrap(JsArrayString array) {
		if (array == null) {
			return null;
		}
		return build(array);
	}
}
