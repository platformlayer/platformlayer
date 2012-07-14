package org.platformlayer.gwt.client.projectlist;

import org.platformlayer.gwt.client.home.HomePlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ProjectListPlace extends ApplicationPlace {
	public static final ProjectListPlace INSTANCE = new ProjectListPlace();

	@Prefix("projects")
	public static class Tokenizer implements PlaceTokenizer<ProjectListPlace> {
		@Override
		public ProjectListPlace getPlace(String token) {
			return INSTANCE;
		}

		@Override
		public String getToken(ProjectListPlace place) {
			return "";
		}
	}

	@Override
	public ApplicationPlace getParent() {
		return HomePlace.build();
	}

	@Override
	public String getLabel() {
		return "Projects";
	}

	public static ProjectListPlace build() {
		return INSTANCE;
	}
}
