package org.platformlayer.gwt.client.signin;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.signup.SignUpPlace;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;
import org.platformlayer.gwt.client.widgets.AlertContainer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class SignInViewImpl extends AbstractApplicationPage implements SignInView {

	interface ViewUiBinder extends UiBinder<HTMLPanel, SignInViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public SignInViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		addClickHandler(signIn, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				alerts.clear();

				boolean rememberMe = true;
				activity.doLogin(username.getValue(), password.getValue(), rememberMe);
			}
		});

		addClickHandler(signupAnchor, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.goTo(SignUpPlace.INSTANCE);
			}
		});
	}

	@UiField
	AlertContainer alerts;

	@UiField
	InputElement username;

	@UiField
	InputElement password;

	@UiField
	ButtonElement signIn;

	@UiField
	AnchorElement signupAnchor;

	private SignInActivity activity;

	@Override
	public void start(final SignInActivity activity) {
		this.activity = activity;
		alerts.clear();
	}

	@Override
	public void showError(Throwable caught) {
		alerts.addError(caught);
	}
}