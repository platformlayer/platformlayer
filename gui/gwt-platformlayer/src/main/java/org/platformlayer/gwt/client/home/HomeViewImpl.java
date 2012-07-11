package org.platformlayer.gwt.client.home;

import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class HomeViewImpl extends AbstractApplicationPage implements HomeView {
	interface HomeViewUiBinder extends UiBinder<HTMLPanel, HomeViewImpl> {
	}

	private static HomeViewUiBinder dashboardViewUiBinder = GWT.create(HomeViewUiBinder.class);

	public HomeViewImpl() {
		initWidget(dashboardViewUiBinder.createAndBindUi(this));
	}

	@Inject
	PlaceController placeController;

	@Override
	public void start(HomeActivity homeActivity) {

	}
}