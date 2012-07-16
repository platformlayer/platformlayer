package org.platformlayer.gwt.client.project;

import org.platformlayer.gwt.client.itemlist.ItemListPlace;
import org.platformlayer.gwt.client.joblist.JobListPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;

public class ProjectPlace extends ApplicationPlace {
	public ProjectPlace(ProjectListPlace projects, String projectKey) {
		super(projects, projectKey);
	}

	@Override
	public String getLabel() {
		return "Project " + getProjectKey();
	}

	public String getProjectKey() {
		return getPathToken();
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		if (pathToken.equals(ItemListPlace.KEY_ROOTS)) {
			return getRootPlace();
		}
		if (pathToken.equals("jobs")) {
			return getJobListPlace();
		}
		return null;
	}

	public JobListPlace getJobListPlace() {
		return new JobListPlace(this);
	}

	public ItemListPlace getRootPlace() {
		return ItemListPlace.buildRoots(this);
	}

	@Override
	public ProjectPlace getProject() {
		return this;
	}

}
