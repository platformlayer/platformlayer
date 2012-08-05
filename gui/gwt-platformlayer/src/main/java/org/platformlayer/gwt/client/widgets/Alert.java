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

	public static Alert success(String message, ApplicationPlace placeLink) {
		Alert alert = new Alert();
		alert.message = message;
		alert.level = AlertLevel.Success;
		alert.placeLink = placeLink;
		return alert;
	}

}
