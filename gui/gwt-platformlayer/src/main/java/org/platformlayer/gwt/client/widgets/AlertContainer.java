package org.platformlayer.gwt.client.widgets;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;

public class AlertContainer extends FlowPanel {
	static final Logger log = Logger.getLogger(AlertContainer.class.getName());

	public static interface AlertTemplates extends SafeHtmlTemplates {
		public static final AlertTemplates INSTANCE = GWT.create(AlertTemplates.class);

		@Template("<button class=\"close\" data-dismiss=\"alert\">×</button>")
		SafeHtml closeButton();
	}

	public void addSuccess(String message, ApplicationPlace placeLink) {
		Element div = DOM.createDiv();
		div.setClassName("alert alert-success");

		SafeHtmlBuilder html = new SafeHtmlBuilder();
		html.append(AlertTemplates.INSTANCE.closeButton());

		html.appendEscaped(message);

		div.setInnerSafeHtml(html.toSafeHtml());

		getElement().appendChild(div);
	}

	public void addError(String message, Throwable e) {
		Element div = DOM.createDiv();
		div.setClassName("alert alert-error");

		SafeHtmlBuilder html = new SafeHtmlBuilder();
		html.append(AlertTemplates.INSTANCE.closeButton());
		html.appendEscaped(message);

		div.setInnerSafeHtml(html.toSafeHtml());

		getElement().appendChild(div);
	}
}
