package org.platformlayer.gwt.client.signup;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.CustomerFacingException;
import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.LoginService;
import org.platformlayer.gwt.client.api.login.RegisterResponse;
import org.platformlayer.gwt.client.api.login.StaticAuthenticationToken;
import org.platformlayer.gwt.client.api.login.Token;
import org.platformlayer.gwt.client.home.HomePlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class SignUpActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(SignUpActivity.class.getName());

	@Inject
	SignUpView view;

	@Inject
	LoginService service;

	SignUpPlace place;

	@Override
	public void init(Place place) {
		this.place = (SignUpPlace) place;
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

	@Override
	public SignUpPlace getPlace() {
		return place;
	}

	public void doRegister(final String username, String password) {
		service.register(username, password, new AsyncCallback<RegisterResponse>() {
			@Override
			public void onSuccess(RegisterResponse result) {
				Access access = null;
				String tokenId = null;

				if (result != null) {
					access = result.getAccess();
					if (access != null) {
						Token token = access.getToken();
						if (token != null) {
							tokenId = token.getId();
						}
					}
				}

				if (tokenId == null) {
					// Not normal ... we should either get a token or a login
					view.showError(new CustomerFacingException("Registration failed unexpectedly"));
				} else {
					// Treat as login
					app.setAuthentication(username, new StaticAuthenticationToken(access), access.getProjects());

					placeController.goTo(HomePlace.INSTANCE);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught);
			}
		});
	}
}