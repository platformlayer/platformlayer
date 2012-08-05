package org.platformlayer.gwt.client.signup;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class SignUpPlace extends ApplicationPlace {
	public SignUpPlace() {
		super(HomePlace.INSTANCE, "signup");
	}

	public static final SignUpPlace INSTANCE = new SignUpPlace();

	@Override
	public String getLabel() {
		return "Sign Up";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

}
