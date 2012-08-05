package org.platformlayer.gwt.client.signin;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.CustomerFacingException;
import org.platformlayer.gwt.client.HttpStatusCodeException;
import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.AuthenticateResponse;
import org.platformlayer.gwt.client.api.login.LoginService;
import org.platformlayer.gwt.client.api.login.StaticAuthenticationToken;
import org.platformlayer.gwt.client.api.login.Token;
import org.platformlayer.gwt.client.home.HomePlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class SignInActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(SignInActivity.class.getName());

	@Inject
	SignInView view;

	@Inject
	LoginService service;

	SignInPlace place;

	@Override
	public void init(Place place) {
		this.place = (SignInPlace) place;
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

	public void doLogin(final String username, String password, boolean rememberMe) {
		service.login(username, password, new AsyncCallback<AuthenticateResponse>() {
			@Override
			public void onSuccess(AuthenticateResponse result) {
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
					view.showError(new CustomerFacingException("Unexpected internal error logging in"));
				} else {
					app.setAuthentication(username, new StaticAuthenticationToken(access), access.getProjects());
					placeController.goTo(HomePlace.INSTANCE);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (HttpStatusCodeException.is401(caught)) {
					// TODO: Show registration link?
					view.showError(new CustomerFacingException("Username or password are incorrect"));
				} else {
					view.showError(caught);
				}
			}
		});
	}

	@Override
	public SignInPlace getPlace() {
		return place;
	}
}