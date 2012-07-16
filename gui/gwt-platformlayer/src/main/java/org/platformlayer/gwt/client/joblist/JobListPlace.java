package org.platformlayer.gwt.client.joblist;

import org.platformlayer.gwt.client.job.JobPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.project.ProjectPlace;

public class JobListPlace extends ApplicationPlace {
	public JobListPlace(ProjectPlace parent) {
		super(parent, "jobs");
	}

	@Override
	public String getLabel() {
		return "Jobs";
	}

	public ProjectPlace getProjectPlace() {
		return getParent();
	}

	@Override
	public ProjectPlace getParent() {
		return (ProjectPlace) super.getParent();
	}

	public String getProjectKey() {
		return getProjectPlace().getProjectKey();
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return getJobPlace(pathToken);
	}

	public JobPlace getJobPlace(String jobId) {
		return new JobPlace(this, jobId);
	}
}
