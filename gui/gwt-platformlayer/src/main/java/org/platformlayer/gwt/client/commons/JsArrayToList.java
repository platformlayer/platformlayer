package org.platformlayer.gwt.client.commons;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsArrayToList<T extends JavaScriptObject> extends JsArrayToListBase<T> {

	final JsArray<T> array;

	public JsArrayToList(JsArray<T> array) {
		super();
		this.array = array;
	}

	@Override
	public int size() {
		return array.length();
	}

	@Override
	public T get(int index) {
		return array.get(index);
	}

	public static <T extends JavaScriptObject> JsArrayToList<T> build(JsArray<T> array) {
		return new JsArrayToList<T>(array);
	}

	public static <T extends JavaScriptObject> JsArrayToList<T> wrap(JsArray<T> array) {
		if (array == null) {
			return null;
		}
		return new JsArrayToList<T>(array);
	}
}
