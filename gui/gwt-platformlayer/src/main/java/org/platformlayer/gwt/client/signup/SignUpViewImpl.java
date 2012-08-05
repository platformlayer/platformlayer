package org.platformlayer.gwt.client.signup;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.signin.SignInPlace;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;
import org.platformlayer.gwt.client.widgets.AlertContainer;
import org.platformlayer.gwt.client.widgets.AlertContainer.AlertLevel;
import org.platformlayer.gwt.client.widgets.ControlGroup;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class SignUpViewImpl extends AbstractApplicationPage implements SignUpView, Editor<SignUpModel> {

	interface ViewUiBinder extends UiBinder<HTMLPanel, SignUpViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	interface Driver extends SimpleBeanEditorDriver<SignUpModel, SignUpViewImpl> {
	}

	Driver driver = GWT.create(Driver.class);

	public SignUpViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		addClickHandler(registerButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				alerts.clear();

				SignUpModel info = driver.flush();
				if (driver.hasErrors()) {
					// A sub-editor reported errors
					// TODO: handle this better
					return;
				}

				// TODO: Use JSR 303
				if (Strings.isNullOrEmpty(info.email)) {
					alerts.add(AlertLevel.Error, "Email is required");
					return;
				}
				if (Strings.isNullOrEmpty(info.password)) {
					alerts.add(AlertLevel.Error, "Password is required");
					return;
				}

				if (!Objects.equal(info.password, info.password2)) {
					alerts.add(AlertLevel.Error, "Passwords do not match");
					return;
				}

				activity.doRegister(info.email, info.password);
			}
		});

		// TODO: Use attributes to do this declaratively? Like jQuery...
		// Or maybe just a go to place helper?
		addClickHandler(loginAnchor, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.goTo(SignInPlace.INSTANCE);
			}
		});
	}

	@UiField
	AnchorElement loginAnchor;

	@UiField
	ControlGroup email;
	@UiField
	ControlGroup password;

	@UiField
	ControlGroup password2;

	@UiField
	ButtonElement registerButton;

	@UiField
	AlertContainer alerts;

	private SignUpActivity activity;

	@Override
	public void start(final SignUpActivity activity) {
		this.activity = activity;

		alerts.clear();

		driver.initialize(this);

		SignUpModel defaults = new SignUpModel();
		driver.edit(defaults);
	}

	@Override
	public void showError(Throwable caught) {
		alerts.addError(caught);
	}
}