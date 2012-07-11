package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.common.collect.Lists;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

public class HeaderActivity extends ApplicationAbstractActivity {

	private final HeaderView headerView;

	public HeaderActivity(PlaceController placeController, Place place, HeaderView headerView) {
		super(placeController, (ApplicationPlace) place);
		this.headerView = headerView;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		List<ApplicationPlace> breadcrumbs = Lists.newArrayList();
		collectBreadcrumbs(place, breadcrumbs);

		headerView.setBreadcrumbs(breadcrumbs);

		panel.setWidget(headerView.asWidget());
	}

	private void collectBreadcrumbs(ApplicationPlace place, List<ApplicationPlace> dest) {
		if (place == null) {
			return;
		}

		ApplicationPlace parent = place.getParent();
		collectBreadcrumbs(parent, dest);

		dest.add(place);
	}

}
