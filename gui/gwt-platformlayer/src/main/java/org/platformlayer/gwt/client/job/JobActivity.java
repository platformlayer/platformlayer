package org.platformlayer.gwt.client.job;

import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.stores.JobStore;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class JobActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(JobActivity.class.getName());

	@Inject
	JobView view;

	JobPlace place;

	@Inject
	JobStore jobStore;

	@Inject
	PlatformLayerService platformLayer;

	@Override
	public void init(Place place) {
		this.place = (JobPlace) place;
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

	public JobPlace getPlace() {
		return place;
	}

	public Job getJobState() {
		return jobStore.getJobState(place.getJobId());
	}
}