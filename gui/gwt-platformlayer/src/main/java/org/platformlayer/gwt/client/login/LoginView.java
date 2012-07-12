package org.platformlayer.gwt.client.login;

import org.platformlayer.gwt.client.view.ApplicationView;

public interface LoginView extends ApplicationView {
	void start(LoginActivity homeActivity);

	void showError(Integer statusCode, Throwable caught);
}
