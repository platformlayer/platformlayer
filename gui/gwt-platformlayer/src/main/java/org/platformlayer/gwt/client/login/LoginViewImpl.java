package org.platformlayer.gwt.client.login;

import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.HTMLPanel;

public class LoginViewImpl extends AbstractApplicationPage implements LoginView {

	interface LoginViewImplBinder extends UiBinder<HTMLPanel, LoginViewImpl> {
	}

	private static LoginViewImplBinder dashboardViewUiBinder = GWT.create(LoginViewImplBinder.class);

	public LoginViewImpl() {
		initWidget(dashboardViewUiBinder.createAndBindUi(this));
	}

	@UiField
	DivElement alert;

	@UiField
	InputElement username;

	@UiField
	InputElement password;

	@UiField
	InputElement rememberMe;

	@UiField
	ButtonElement signIn;

	@Override
	protected void onAttach() {
		super.onAttach();
	}

	@Override
	public void start(final LoginActivity activity) {
		alert.getStyle().setVisibility(Visibility.HIDDEN);

		// Elements.asJsElement(signIn).setOnclick(new EventListener() {
		// @Override
		// public void handleEvent(Event event) {
		//
		// // event.setReturnValue(false);
		// }
		// });

		DOM.setEventListener(signIn.<com.google.gwt.user.client.Element> cast(), new EventListener() {

			@Override
			public void onBrowserEvent(Event event) {
				boolean rememberMeChecked = "1".equals(rememberMe.getValue());
				activity.doLogin(username.getValue(), password.getValue(), rememberMeChecked);
				event.preventDefault();
			}
		});

		DOM.sinkEvents(signIn.<com.google.gwt.user.client.Element> cast(), Event.ONCLICK);
		// >
		// > Also consider adding a Window CloseHandler to prevent the memory leak.
		// > Window.addCloseHandler(new CloseHandler<Window>() {
		// > public void onClose(CloseEvent<Window> event)
		// > {
		// > DOM.setEventListener(buttonElement, null);
		// > }
		// >
		// > });

		// if (signInElement == null) {
		// throw new IllegalStateException("NULL SIGNIN");
		// }
		//
		// signInElement.addEventListener(Event.CLICK, new EventListener() {
		// @Override
		// public void handleEvent(Event evt) {
		// }
		// }, false);
	}

	enum AlertLevel {
		Error, Success, Info
	}

	@Override
	public void showError(Integer statusCode, Throwable caught) {
		AlertLevel level = AlertLevel.Error;
		String message;

		if (caught != null) {
			message = "An error occurred while logging in";
		} else {
			int code = 0;
			if (statusCode != null) {
				code = statusCode;
			}

			if (code == 401) {
				message = "Username or password are incorrect";
				level = AlertLevel.Info;
			} else {
				message = "An unknown error occurred while logging in";
			}
		}

		SafeHtml messageHtml = SafeHtmlUtils.fromString(message);

		addOrRemoveClassName(level == AlertLevel.Error, alert, "alert-error");
		addOrRemoveClassName(level == AlertLevel.Success, alert, "alert-success");
		addOrRemoveClassName(level == AlertLevel.Info, alert, "alert-info");

		alert.setInnerHTML(messageHtml.asString());
		alert.getStyle().setVisibility(Visibility.VISIBLE);
	}

	private static void addOrRemoveClassName(boolean add, DivElement div, String className) {
		if (add) {
			div.addClassName(className);
		} else {
			div.removeClassName(className);
		}
	}
}