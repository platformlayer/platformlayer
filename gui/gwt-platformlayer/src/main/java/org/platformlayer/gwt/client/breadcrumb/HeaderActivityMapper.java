package org.platformlayer.gwt.client.breadcrumb;

import org.platformlayer.gwt.client.ApplicationGinjector;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

public class HeaderActivityMapper implements ActivityMapper {

	@Inject
	ApplicationGinjector injector;

	@Override
	public Activity getActivity(Place place) {
		HeaderActivity activity = injector.getHeaderActivity();
		activity.init(place);
		return activity;
	}

}