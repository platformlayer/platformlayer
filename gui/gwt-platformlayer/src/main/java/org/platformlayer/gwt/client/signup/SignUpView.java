package org.platformlayer.gwt.client.signup;

import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(SignUpViewImpl.class)
public interface SignUpView extends ApplicationView {
	void start(SignUpActivity homeActivity);

	void showError(Throwable caught);
}
