package org.platformlayer.gwt.client.job;

import java.util.List;

import org.platformlayer.gwt.client.api.platformlayer.Job;
import org.platformlayer.gwt.client.api.platformlayer.JobLogLine;
import org.platformlayer.gwt.client.view.ApplicationView;

import com.google.inject.ImplementedBy;

@ImplementedBy(JobViewImpl.class)
public interface JobView extends ApplicationView {
	void start(JobActivity activity);

	/**
	 * Update the job state (but not the log)
	 */
	void setJobData(Job job);

	void updateJobLog(List<JobLogLine> lines);

}
