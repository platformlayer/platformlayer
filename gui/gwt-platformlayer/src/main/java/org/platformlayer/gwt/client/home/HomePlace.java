package org.platformlayer.gwt.client.home;

import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;
import org.platformlayer.gwt.client.signin.SignInPlace;

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

		if (pathToken.equals(SignInPlace.INSTANCE.getPathToken())) {
			return SignInPlace.INSTANCE;
		}

		return null;
	}
}
