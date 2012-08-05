package org.platformlayer.gwt.client.widgets;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.CustomerFacingException;
import org.platformlayer.gwt.client.HttpStatusCodeException;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
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
		SafeHtml anchor(SafeUri href, String label);
	}

	public AlertContainer() {
		sinkEvents(Event.getTypeInt("click"));
	}

	public void add(AlertLevel level, String message, ApplicationPlace placeLink, Throwable e) {
		Element div = DOM.createDiv();

		String className;

		switch (level) {
		case Error:
			className = "alert alert-error";
			break;

		case Success:
			className = "alert alert-success";
			break;
		case Info:
			className = "alert alert-info";
			break;

		default:
			throw new IllegalStateException();
		}

		div.setClassName(className);

		SafeHtmlBuilder html = new SafeHtmlBuilder();
		html.append(AlertTemplates.INSTANCE.closeButton());
		html.appendEscaped(message);

		if (placeLink != null) {
			String token = ApplicationState.get().getToken(placeLink);
			SafeUri uri = UriUtils.fromString("#" + token);
			html.append(AlertTemplates.INSTANCE.anchor(uri, "Open"));
		}

		div.setInnerSafeHtml(html.toSafeHtml());

		getElement().appendChild(div);
	}

	public void addError(Throwable e) {
		String message = null;
		AlertLevel level = AlertLevel.Error;

		if (e instanceof CustomerFacingException) {
			message = e.getMessage();
			level = ((CustomerFacingException) e).getAlertLevel();
		} else if (e instanceof HttpStatusCodeException) {
			int statusCode = ((HttpStatusCodeException) e).getStatusCode();

			switch (statusCode) {
			case 0:
				message = "Error communicating with the server.  Are you online?";
				break;

			case CustomerFacingException.STATUS_CODE:
				message = ((HttpStatusCodeException) e).getStatusText();
				if (message.isEmpty()) {
					message = null;
				}
				break;

			case 401:
				message = "You don't have permission to do that.";
				break;
			case 500:
				message = "The server had an unexpected error processing your request.  Please try again.";
				break;
			}
		}
		if (message == null) {
			message = "An unexpected error occurred";
		}
		add(level, message, null, e);
	}

	public enum AlertLevel {
		Error, Success, Info
	}

	public void add(AlertLevel level, String message) {
		add(level, message, null, null);
	}

	public void addError(String message, Throwable e) {
		add(AlertLevel.Error, message);
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

	@Override
	public void clear() {
		NodeList<Node> children = getElement().getChildNodes();
		for (int i = children.getLength(); i >= 0; i--) {
			Node child = children.getItem(i);

			com.google.gwt.dom.client.Element current = child.cast();
			String className = current.getClassName();

			if (className != null && className.contains("alert")) {
				current.removeFromParent();
			}
		}
	}

	public void add(Alert flash) {
		add(flash.level, flash.message, flash.placeLink, flash.e);
	}
}
