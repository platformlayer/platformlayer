package org.platformlayer.gwt.client.breadcrumb;

import java.util.List;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.common.collect.Lists;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class HeaderActivity extends ApplicationAbstractActivity {

	@Inject
	HeaderView headerView;

	ApplicationPlace place;

	@Override
	public void init(Place place) {
		this.place = (ApplicationPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		List<ApplicationPlace> breadcrumbs = Lists.newArrayList();
		collectBreadcrumbs(place, breadcrumbs);

		headerView.setBreadcrumbs(breadcrumbs);

		panel.setWidget(headerView.asWidget());
	}

	private void collectBreadcrumbs(ApplicationPlace place, List<ApplicationPlace> dest) {
		// TODO: Better to add non-recursively and then reverse??
		if (place == null) {
			return;
		}

		ApplicationPlace parent = place.getParent();
		collectBreadcrumbs(parent, dest);

		dest.add(place);
	}

}
