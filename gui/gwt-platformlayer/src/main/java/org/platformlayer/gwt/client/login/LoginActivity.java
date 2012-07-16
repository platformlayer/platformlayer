package org.platformlayer.gwt.client.login;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.login.Access;
import org.platformlayer.gwt.client.api.login.AuthenticateResponse;
import org.platformlayer.gwt.client.api.login.LoginService;
import org.platformlayer.gwt.client.api.login.StaticAuthenticationToken;
import org.platformlayer.gwt.client.api.login.Token;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class LoginActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(LoginActivity.class.getName());

	@Inject
	LoginView view;

	@Inject
	LoginService service;

	LoginPlace place;

	@Override
	public void init(Place place) {
		this.place = (LoginPlace) place;
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

	public void doLogin(String username, String password, boolean rememberMe) {
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
					int statusCode = result != null ? result.getStatusCode() : null;
					view.showError(statusCode, null);
				} else {
					app.setAuthentication(new StaticAuthenticationToken(access), access.getProjects());
					placeController.goTo(ProjectListPlace.INSTANCE);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				view.showError(null, caught);
			}
		});
	}

	@Override
	public LoginPlace getPlace() {
		return place;
	}
}