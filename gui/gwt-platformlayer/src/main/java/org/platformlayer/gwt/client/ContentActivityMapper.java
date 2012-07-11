package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.home.HomeActivity;
import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.home.HomeView;
import org.platformlayer.gwt.client.login.LoginActivity;
import org.platformlayer.gwt.client.login.LoginPlace;
import org.platformlayer.gwt.client.login.LoginView;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Inject;

public class ContentActivityMapper implements ActivityMapper {

	@Inject
	private PlaceController placeController;

	@Inject
	private HomeView homeView;

	@Inject
	private LoginView loginView;

	private Boolean init = false;

	@Inject
	public ContentActivityMapper() {
		super();
	}

	/**
	 * Implement all logic for place changing here
	 */
	@Override
	public Activity getActivity(Place place) {
		if (init == false) {
			initEventsAndHandlers();
			init = true;
		}

		if (place instanceof LoginPlace) {
			return new LoginActivity((LoginPlace) place, loginView, placeController);
		}
		if (place instanceof HomePlace) {
			return new HomeActivity((HomePlace) place, homeView, placeController);
		}
		return null;
	}

	/**
	 * Wire all click handlers for singleton views here
	 */
	private void initEventsAndHandlers() {

	}

}