package org.platformlayer.gwt.client.home;

import org.platformlayer.gwt.client.login.LoginPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;

public class HomePlace extends ApplicationPlace {
	public HomePlace() {
		super(null, "");
	}

	public static final HomePlace INSTANCE = new HomePlace();

	@Override
	public String getLabel() {
		return "Home";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		if (pathToken.equals(ProjectListPlace.INSTANCE.getPathToken())) {
			return ProjectListPlace.INSTANCE;
		}

		if (pathToken.equals(LoginPlace.INSTANCE.getPathToken())) {
			return LoginPlace.INSTANCE;
		}

		return null;
	}
}
