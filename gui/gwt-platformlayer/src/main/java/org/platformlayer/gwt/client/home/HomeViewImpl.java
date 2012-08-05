package org.platformlayer.gwt.client.home;

import javax.inject.Singleton;

import org.platformlayer.gwt.client.accountsummary.AccountSummaryPlace;
import org.platformlayer.gwt.client.view.AbstractApplicationPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

@Singleton
public class HomeViewImpl extends AbstractApplicationPage implements HomeView {
	interface ViewUiBinder extends UiBinder<HTMLPanel, HomeViewImpl> {
	}

	private static ViewUiBinder viewUiBinder = GWT.create(ViewUiBinder.class);

	public HomeViewImpl() {
		initWidget(viewUiBinder.createAndBindUi(this));

		addClickHandler(myAccountButton, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				activity.goTo(AccountSummaryPlace.INSTANCE);
			}
		});
	}

	@UiField
	AnchorElement myAccountButton;

	private HomeActivity activity;

	@Override
	public void start(HomeActivity activity) {
		this.activity = activity;
	}
}