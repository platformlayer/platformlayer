package org.platformlayer.gwt.client.widgets;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;

public class AlertContainer extends FlowPanel {
	static final Logger log = Logger.getLogger(AlertContainer.class.getName());

	public static interface AlertTemplates extends SafeHtmlTemplates {
		public static final AlertTemplates INSTANCE = GWT.create(AlertTemplates.class);

		@Template("<button class=\"close\" data-dismiss=\"alert\">×</button>")
		SafeHtml closeButton();

		@Template("<a href=\"{0}\">{1}</a>")
		SafeHtml anchor(String href, String label);
	}

	public AlertContainer() {
		sinkEvents(Event.getTypeInt("click"));
	}

	public void addSuccess(String message, ApplicationPlace placeLink) {
		Element div = DOM.createDiv();
		div.setClassName("alert alert-success");

		SafeHtmlBuilder html = new SafeHtmlBuilder();
		html.append(AlertTemplates.INSTANCE.closeButton());

		html.appendEscaped(message);

		if (placeLink != null) {
			String token = ApplicationState.get().getToken(placeLink);
			html.append(AlertTemplates.INSTANCE.anchor("#" + token, "Open"));
		}

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

	@Override
	public void onBrowserEvent(Event event) {
		super.onBrowserEvent(event);

		// TODO: Cleanup or convert to generator??
		// TODO: is Event.getTypeInt inlined?
		if (event.getTypeInt() == Event.getTypeInt("click")) {
			EventTarget eventTarget = event.getEventTarget();
			if (Element.is(eventTarget)) {
				Element target = eventTarget.cast();
				String dismiss = target.getAttribute("data-dismiss");
				if (!Strings.isNullOrEmpty(dismiss)) {
					doDismiss(target, dismiss);
					event.preventDefault();
				}
			}
		}
	}

	private boolean doDismiss(Element target, String dismiss) {
		// TODO: Use GwtQuery here?
		// TODO: I think Twitter Bootstrap just hard-codes data-dismiss=modal; the logic isn't the same
		String find = " " + dismiss + " ";

		com.google.gwt.dom.client.Element current = target;
		while (current != null) {
			String className = current.getClassName();

			if (!Strings.isNullOrEmpty(className)) {
				className = " " + className + " ";

				if (className.contains(find)) {
					current.removeFromParent();
					return true;
				}
			}

			current = current.getParentElement();
		}

		log.warning("Could not find element to dismiss: " + dismiss);
		return false;
	}
}
