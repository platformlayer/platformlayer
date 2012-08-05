package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.widgets.Alert;
import org.platformlayer.gwt.client.widgets.AlertContainer;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
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

	@UiField
	AlertContainer alerts;

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

		List<Element> items = Lists.newArrayList();

		for (int i = 0; i < breadcrumbs.size(); i++) {
			ApplicationPlace breadcrumb = breadcrumbs.get(i);

			String label = breadcrumb.getLabel();
			String targetHistoryToken = placeHistoryMapper.getToken(breadcrumb);

			Element li = DOM.createElement("li");
			Element a = DOM.createAnchor();
			a.setInnerText(label);
			a.setAttribute("href", "#" + targetHistoryToken);

			li.appendChild(a);
			items.add(li);
		}

		control.setBreadcrumbs(items);

		breadcrumbPanel.clear();
		breadcrumbPanel.add(control);
	}

	@UiHandler("homeButton")
	public void homeButtonClicked(ClickEvent e) {
		placeController.goTo(HomePlace.INSTANCE);
	}

	@Override
	public void setFlash(Alert flash) {
		alerts.clear();

		if (flash != null) {
			alerts.add(flash);
		}
	}
}