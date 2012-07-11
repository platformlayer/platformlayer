package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HeaderViewImpl extends Composite implements HeaderView {

	interface HeaderViewUiBinder extends UiBinder<HTMLPanel, HeaderViewImpl> {
	}

	private static HeaderViewUiBinder headerViewUiBinder = GWT.create(HeaderViewUiBinder.class);

	@UiField
	FlowPanel breadcrumbPanel;

	public HeaderViewImpl() {
		initWidget(headerViewUiBinder.createAndBindUi(this));
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Inject
	PlaceHistoryMapper placeHistoryMapper;

	@Inject
	PlaceController placeController;

	@Override
	public void setBreadcrumbs(List<ApplicationPlace> breadcrumbs) {
		BootstrapBreadcrumbs control = new BootstrapBreadcrumbs();

		for (int i = 0; i < breadcrumbs.size(); i++) {
			ApplicationPlace breadcrumb = breadcrumbs.get(i);

			String targetHistoryToken = placeHistoryMapper.getToken(breadcrumb);
			NavLink placeNavWidget = new NavLink();
			placeNavWidget.setTargetHistoryToken(targetHistoryToken);

			String text = breadcrumb.getLabel();
			placeNavWidget.setText(text);

			control.add(placeNavWidget);
		}

		breadcrumbPanel.clear();
		breadcrumbPanel.add(control);
	}

	@UiHandler("homeButton")
	public void homeButtonClicked(ClickEvent e) {
		placeController.goTo(HomePlace.build());
	}
}