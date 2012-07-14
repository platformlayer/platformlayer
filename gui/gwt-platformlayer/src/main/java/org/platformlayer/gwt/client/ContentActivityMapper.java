package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.login.LoginPlace;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

public class ContentActivityMapper implements ActivityMapper {
	@Inject
	ApplicationGinjector injector;

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

		ApplicationAbstractActivity activity = null;

		if (place instanceof HomePlace) {
			activity = injector.getHomeActivity();
		} else if (place instanceof ProjectListPlace) {
			activity = injector.getProjectListActivity();
		} else if (place instanceof LoginPlace) {
			activity = injector.getLoginActivity();
		}

		if (activity == null) {
			throw new IllegalStateException();
		}

		activity.init(place);
		return activity;
	}

	/**
	 * Wire all click handlers for singleton views here
	 */
	private void initEventsAndHandlers() {

	}

}