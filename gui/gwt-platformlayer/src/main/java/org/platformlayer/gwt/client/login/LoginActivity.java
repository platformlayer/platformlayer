package org.platformlayer.gwt.client.login;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

public class LoginActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(LoginActivity.class.getName());

	private final LoginView view;

	final LoginPlace place;

	public LoginActivity(LoginPlace place, LoginView view, PlaceController placeController) {
		super(placeController, place);
		this.place = place;
		this.view = view;
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