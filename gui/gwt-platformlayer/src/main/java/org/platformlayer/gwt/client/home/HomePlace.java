package org.platformlayer.gwt.client.home;

import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class HomePlace extends ApplicationPlace {
	public static final HomePlace INSTANCE = new HomePlace();

	@Prefix("home")
	public static class Tokenizer implements PlaceTokenizer<HomePlace> {
		@Override
		public HomePlace getPlace(String token) {
			return INSTANCE;
		}

		@Override
		public String getToken(HomePlace place) {
			return "";
		}
	}

	@Override
	public ApplicationPlace getParent() {
		return null;
	}

	@Override
	public String getLabel() {
		return "Home";
	}

	public static HomePlace build() {
		return INSTANCE;
	}
}
