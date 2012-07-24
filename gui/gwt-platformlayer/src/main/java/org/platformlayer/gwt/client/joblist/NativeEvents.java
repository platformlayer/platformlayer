package org.platformlayer.gwt.client.joblist;

import com.google.gwt.dom.client.NativeEvent;

public class NativeEvents {
	public static boolean isClick(NativeEvent event) {
		return "click".equals(event.getType());
	}
}
