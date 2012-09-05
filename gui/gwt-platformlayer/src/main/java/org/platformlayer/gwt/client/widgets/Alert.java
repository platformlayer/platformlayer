package org.platformlayer.gwt.client.widgets;

import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.widgets.AlertContainer.AlertLevel;

public class Alert {
	public AlertLevel level;
	public String message;
	public ApplicationPlace placeLink;
	public Throwable e;

	public static Alert success(String message) {
		return success(message, null);
	}

	public static Alert error(String message) {
		return error(message, null);
	}

	public static Alert info(String message) {
		return info(message, null);
	}

	public static Alert error(String message, ApplicationPlace placeLink) {
		return build(AlertLevel.Error, message, placeLink);
	}

	public static Alert info(String message, ApplicationPlace placeLink) {
		return build(AlertLevel.Info, message, placeLink);
	}

	public static Alert success(String message, ApplicationPlace placeLink) {
		return build(AlertLevel.Success, message, placeLink);
	}

	public static Alert build(AlertLevel level, String message, ApplicationPlace placeLink) {
		Alert alert = new Alert();
		alert.message = message;
		alert.level = level;
		alert.placeLink = placeLink;
		return alert;
	}

}
