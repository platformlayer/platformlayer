package org.platformlayer.gwt.client;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.job.JobPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public abstract class ApplicationAbstractActivity extends AbstractActivity {

	@Inject
	protected PlaceController placeController;

	@Inject
	protected ApplicationState app;

	public abstract void start(AcceptsOneWidget panel, EventBus eventBus);

	public abstract void init(Place place);

	protected abstract ApplicationPlace getPlace();

	@Override
	public void start(AcceptsOneWidget panel, com.google.gwt.event.shared.EventBus eventBus) {
		start(panel, (EventBus) eventBus);
	}

	/**
	 * Navigate to a new Place in the browser
	 */
	public void goTo(Place place) {
		placeController.goTo(place);
	}

	/**
	 * Ask user before stopping this activity
	 */
	@Override
	public String mayStop() {
		return null;
	}

	protected String getModuleUrl() {
		String moduleUrl = Location.getHref();

		if (moduleUrl.indexOf("#") != -1) {
			moduleUrl = moduleUrl.substring(0, Location.getHref().indexOf("#"));
		}

		return moduleUrl;
	}

	public OpsProject getProject() {
		ApplicationPlace place = getPlace();
		OpsProject project = app.findProject(place);
		if (project == null) {
			project = app.getUserProject();
		}
		assert project != null;
		return project;
	}

	public JobPlace getJobPlace(Job job) {
		return new JobPlace(getPlace().getProject().getJobListPlace(), job.getJobId());
	}
}