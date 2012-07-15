package org.platformlayer.gwt.client.login;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class LoginPlace extends ApplicationPlace {
	public LoginPlace() {
		super(HomePlace.INSTANCE, "login");
	}

	public static final LoginPlace INSTANCE = new LoginPlace();

	@Override
	public String getLabel() {
		return "Login";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

}
