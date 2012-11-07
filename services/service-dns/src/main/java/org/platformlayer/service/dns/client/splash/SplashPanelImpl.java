package org.platformlayer.service.dns.client.splash;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.ApplicationState;
import org.platformlayer.gwt.client.places.ShellPlace;
import org.platformlayer.service.dns.client.home.HomePlace;
import org.platformlayer.ui.shared.client.views.AbstractApplicationView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class SplashPanelImpl extends AbstractApplicationView implements SplashPanel {
	interface ViewUiBinder extends UiBinder<HTMLPanel, SplashPanelImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	@UiField
	Button goButton;

	@Inject
	ApplicationState app;

	private ShellPlace parent;

	public SplashPanelImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));
	}

	@UiHandler("goButton")
	public void onGoButtonClick(ClickEvent e) {
		HomePlace root = new HomePlace(parent);
		app.goTo(root);
	}

	@Override
	public void init(Place parent) {
		this.parent = (ShellPlace) parent;
	}

}