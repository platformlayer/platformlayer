package org.platformlayer.gwt.client.login;

import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

public class LoginViewImpl extends AbstractApplicationPage implements LoginView {

	interface HomeViewUiBinder extends UiBinder<HTMLPanel, LoginViewImpl> {
	}

	private static HomeViewUiBinder dashboardViewUiBinder = GWT.create(HomeViewUiBinder.class);

	public LoginViewImpl() {
		initWidget(dashboardViewUiBinder.createAndBindUi(this));
	}

	@Inject
	PlaceController placeController;

	@Override
	public void start(LoginActivity homeActivity) {

	}
}