package org.platformlayer.gwt.client.job;

import org.platformlayer.gwt.client.joblist.JobListPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class JobPlace extends ApplicationPlace {
	public JobPlace(JobListPlace parent, String jobId) {
		super(parent, jobId);
	}

	@Override
	public String getLabel() {
		return "Job " + getJobId();
	}

	public String getJobId() {
		return getPathToken();
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}
}
