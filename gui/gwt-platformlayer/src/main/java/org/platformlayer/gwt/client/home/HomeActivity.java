package org.platformlayer.gwt.client.home;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class HomeActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(HomeActivity.class.getName());

	@Inject
	HomeView view;

	HomePlace place;

	@Override
	public void init(Place place) {
		this.place = (HomePlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}