package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;

public class BootstrapBreadcrumbs extends ComplexPanel {

	public BootstrapBreadcrumbs() {
		setElement(DOM.createElement("ul"));

		setStyleName("breadcrumb");
	}

	public void setBreadcrumbs(List<Element> items) {
		Element element = getElement();

		int size = items.size();
		for (int i = 0; i < size; i++) {
			Element li = items.get(i);

			if (i != 0) {
				Element span = DOM.createSpan();
				span.addClassName("divider");
				element.appendChild(span);
			}

			if ((i + 1) == size) {
				// Last
				li.addClassName("active");
			}

			element.appendChild(li);
		}
	}

}
