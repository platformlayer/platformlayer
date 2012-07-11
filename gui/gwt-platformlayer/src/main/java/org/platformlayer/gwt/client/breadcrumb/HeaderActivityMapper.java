package org.platformlayer.gwt.client.breadcrumb;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Inject;

public class HeaderActivityMapper implements ActivityMapper {

	@Inject
	PlaceController placeController;

	@Inject
	HeaderView headerView;

	@Override
	public Activity getActivity(Place place) {
		return new HeaderActivity(placeController, place, headerView);
	}

}