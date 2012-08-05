package org.platformlayer.gwt.client.signin;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class SignInPlace extends ApplicationPlace {
	public SignInPlace() {
		super(HomePlace.INSTANCE, "signin");
	}

	public static final SignInPlace INSTANCE = new SignInPlace();

	@Override
	public String getLabel() {
		return "Sign In";
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

}
