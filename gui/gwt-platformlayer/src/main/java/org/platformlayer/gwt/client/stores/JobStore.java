package org.platformlayer.gwt.client.stores;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobCollection;
import org.platformlayer.gwt.client.api.platformlayer.JobLog;
import org.platformlayer.gwt.client.api.platformlayer.OpsProject;
import org.platformlayer.gwt.client.api.platformlayer.PlatformLayerService;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.rpc.AsyncCallback;

@Singleton
public class JobStore {
	static final Logger log = Logger.getLogger(JobStore.class.getName());

	final Map<String, Job> jobs = Maps.newHashMap();

	@Inject
	PlatformLayerService platformLayer;

	public Job getJobState(String jobId) {
		// TODO: Fetch on demand, move to async?
		Job job = jobs.get(jobId);
		return job;
	}

	public void listJobs(OpsProject project, final AsyncCallback<JobCollection> callback) {
		platformLayer.listJobs(project, new AsyncCallback<JobCollection>() {

			@Override
			public void onSuccess(JobCollection result) {
				for (Job job : result.getJobs()) {
					jobs.put(job.getJobId(), job);
				}

				callback.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	public void getJobLog(OpsProject project, String jobId, final AsyncCallback<JobLog> callback) {
		platformLayer.getJobLog(project, jobId, new AsyncCallback<JobLog>() {
			@Override
			public void onSuccess(JobLog result) {
				callback.onSuccess(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
