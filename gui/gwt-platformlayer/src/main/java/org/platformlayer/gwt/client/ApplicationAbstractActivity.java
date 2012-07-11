package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

public abstract class ApplicationAbstractActivity extends AbstractActivity {

	protected PlaceController placeController;
	protected ApplicationPlace place;

	public ApplicationAbstractActivity(PlaceController placeController, ApplicationPlace place) {
		this.placeController = placeController;
		this.place = place;
	}

	public abstract void start(AcceptsOneWidget panel, EventBus eventBus);

	@Override
	public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
		start(panel, (EventBus) eventBus);
	}

	/**
	 * Navigate to a new Place in the browser
	 */
	public void goTo(Place place) {
		placeController.goTo(place);
	}

	/**
	 * Ask user before stopping this activity
	 */
	@Override
	public String mayStop() {
		return null;
	}

	protected String getModuleUrl() {
		String moduleUrl = Location.getHref();

		if (moduleUrl.indexOf("#") != -1) {
			moduleUrl = moduleUrl.substring(0, Location.getHref().indexOf("#"));
		}

		return moduleUrl;
	}

}