package org.platformlayer.gwt.client.project;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class ProjectActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(ProjectActivity.class.getName());

	@Inject
	ProjectView view;

	ProjectPlace place;

	@Inject
	PlatformLayerService platformLayer;

	@Override
	public void init(Place place) {
		this.place = (ProjectPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	public ProjectPlace getPlace() {
		return place;
	}
}