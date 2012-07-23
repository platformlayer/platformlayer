package org.platformlayer.gwt.client.job;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.platformlayer.gwt.client.ApplicationAbstractActivity;
import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobLog;
import org.platformlayer.gwt.client.api.platformlayer.JobLogLine;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;
import org.platformlayer.gwt.client.stores.JobStore;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

	JobLogRefresher refresher;

	@Override
	public void init(Place place) {
		this.place = (JobPlace) place;
	}

	@Override
	public void start(AcceptsOneWidget panel, final EventBus eventBus) {
		view.start(this);

		panel.setWidget(view.asWidget());

		String jobId = place.getJobId();
		Job job = jobStore.checkCache(jobId);
		if (job != null) {
			view.setJobData(job);
		} else {
			view.setJobData(null);
		}

		refresher = new JobLogRefresher(getProject(), jobId);
		refresher.start();
	}

	class JobLogRefresher {
		private static final int POLL_INTERVAL = 1000;

		int haveLogLineCount = 0;
		Timer timer;

		final OpsProject project;
		final String jobId;

		public JobLogRefresher(OpsProject project, String jobId) {
			super();
			this.project = project;
			this.jobId = jobId;
		}

		public void start() {
			timer = new Timer() {
				@Override
				public void run() {
					runQuery();
				}
			};

			timer.run();
		}

		public void stop() {
			timer.cancel();
			timer = null;
		}

		void runQuery() {
			int skipJobLines = haveLogLineCount;
			String tree = null; // Everything
			jobStore.getJob(project, jobId, tree, skipJobLines, new AsyncCallback<Job>() {
				@Override
				public void onSuccess(Job result) {
					view.setJobData(result);

					JobLog log = result.getLog();
					List<JobLogLine> lines = log.getLines();
					view.updateJobLog(lines);

					haveLogLineCount += lines.size();

					if (timer != null) {
						if (result.isRunning()) {
							timer.schedule(POLL_INTERVAL);
						}
					}
				}

				@Override
				public void onFailure(Throwable caught) {
					log.log(Level.WARNING, "Error reading job log", caught);

					if (timer != null) {
						timer.schedule(1000);
					}
				}
			});
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		refresher.stop();
	}

	@Override
	public JobPlace getPlace() {
		return place;
	}
}