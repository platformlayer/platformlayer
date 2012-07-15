package org.platformlayer.gwt.client.projectlist;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.project.ProjectPlace;

public class ProjectListPlace extends ApplicationPlace {
	public static final ProjectListPlace INSTANCE = new ProjectListPlace();

	public ProjectListPlace() {
		super(HomePlace.INSTANCE, "projects");
	}

	@Override
	public String getLabel() {
		return "Projects";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return getProjectPlace(pathToken);
	}

	public ProjectPlace getProjectPlace(String projectName) {
		return new ProjectPlace(this, projectName);
	}
}
