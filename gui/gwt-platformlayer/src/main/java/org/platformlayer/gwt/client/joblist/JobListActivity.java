package org.platformlayer.gwt.client.joblist;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobCollection;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.stores.JobStore;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class JobListActivity extends ApplicationAbstractActivity {
	static final Logger log = Logger.getLogger(JobListActivity.class.getName());

	@Inject
	JobListView view;

	JobListPlace place;

	@Inject
	JobStore jobStore;

	@Override
	public void init(Place place) {
		this.place = (JobListPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());

		OpsProject project = app.findProject(place.getProjectKey());

		jobStore.listJobs(project, new AsyncCallback<JobCollection>() {
			@Override
			public void onSuccess(JobCollection result) {
				CellList<Job> jobList = view.getJobList();

				ListDataProvider<Job> provider = new ListDataProvider<Job>(result.getJobs());
				provider.addDataDisplay(jobList);
			}

			@Override
			public void onFailure(Throwable caught) {
				log.log(Level.WARNING, "Error listing jobs", caught);
			}
		});
	}

	public JobListPlace getPlace() {
		return place;
	}

}