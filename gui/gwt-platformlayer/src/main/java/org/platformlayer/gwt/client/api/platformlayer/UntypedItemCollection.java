package org.platformlayer.gwt.client.api.platformlayer;

import java.util.List;

import org.platformlayer.gwt.client.commons.JsArrayToList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class UntypedItemCollection extends JavaScriptObject {
	protected UntypedItemCollection() {
	}

	public final native JsArray<UntypedItem> getItems0()
	/*-{ return this.items; }-*/;

	public final List<UntypedItem> getItems() {
		return JsArrayToList.wrap(getItems0());
	}
}
