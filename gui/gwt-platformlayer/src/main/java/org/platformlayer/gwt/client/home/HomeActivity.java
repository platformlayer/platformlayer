package org.platformlayer.gwt.client.home;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

public class HomeActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(HomeActivity.class.getName());

	private final HomeView view;

	final HomePlace place;

	public HomeActivity(HomePlace place, HomeView view, PlaceController placeController) {
		super(placeController, place);
		this.place = place;
		this.view = view;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());
	}

}