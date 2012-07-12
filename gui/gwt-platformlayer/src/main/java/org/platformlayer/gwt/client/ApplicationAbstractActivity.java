package org.platformlayer.gwt.client;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public abstract class ApplicationAbstractActivity extends AbstractActivity {

	@Inject
	protected PlaceController placeController;

	public abstract void start(AcceptsOneWidget panel, EventBus eventBus);

	public abstract void init(Place place);

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