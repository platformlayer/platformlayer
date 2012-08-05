package org.platformlayer.gwt.client.signin;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(SignInViewImpl.class)
public interface SignInView extends ApplicationView {
	void start(SignInActivity homeActivity);

	void showError(Throwable caught);
}
