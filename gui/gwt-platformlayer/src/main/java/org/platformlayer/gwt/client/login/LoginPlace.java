package org.platformlayer.gwt.client.login;

import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class LoginPlace extends ApplicationPlace {
	public static final LoginPlace INSTANCE = new LoginPlace();

	@Prefix("login")
	public static class Tokenizer implements PlaceTokenizer<LoginPlace> {
		@Override
		public LoginPlace getPlace(String token) {
			return INSTANCE;
		}

		@Override
		public String getToken(LoginPlace place) {
			return "";
		}
	}

	@Override
	public ApplicationPlace getParent() {
		return null;
	}

	@Override
	public String getLabel() {
		return "Login";
	}

	public static LoginPlace build() {
		return INSTANCE;
	}
}
