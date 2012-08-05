package org.platformlayer.gwt.client;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Provider;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class ApplicationPlaceHistoryMapper implements PlaceHistoryMapper {
	public static class Factory implements Provider<ApplicationPlaceHistoryMapper> {
		@Override
		public ApplicationPlaceHistoryMapper get() {
			HomePlace root = HomePlace.INSTANCE;
			ApplicationPlaceHistoryMapper historyMapper = new ApplicationPlaceHistoryMapper(root);
			return historyMapper;
		}
	}

	final HomePlace root;

	public ApplicationPlaceHistoryMapper(HomePlace root) {
		this.root = root;
	}

	private static final char SEPARATOR = '~';

	@Override
	public Place getPlace(String token) {
		ApplicationPlace place = null;

		for (String pathToken : Splitter.on(SEPARATOR).split(token)) {
			if (place == null) {
				place = root;
			} else {
				place = place.getChild(pathToken);
			}

			if (place == null)
				return null;
		}

		return place;
	}

	@Override
	public String getToken(Place placeObject) {
		ApplicationPlace place = (ApplicationPlace) placeObject;

		ArrayList<String> pathTokens = Lists.newArrayList();
		while (true) {
			String pathToken = place.getPathToken();
			if (pathToken == null)
				pathToken = "";

			pathTokens.add(pathToken);
			place = place.getParent();
			if (place == null)
				break;
		}

		Collections.reverse(pathTokens);

		return Joiner.on(SEPARATOR).join(pathTokens);
	}

}