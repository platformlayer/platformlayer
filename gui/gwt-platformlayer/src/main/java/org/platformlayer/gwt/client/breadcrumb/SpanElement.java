package org.platformlayer.gwt.client.breadcrumb;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class SpanElement extends ComplexPanel {
	public SpanElement() {
		setElement(DOM.createSpan());
	}

	@Override
	public void add(Widget w) {
		add(w, getElement());
	}

}
