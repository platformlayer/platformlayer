package org.platformlayer.gwt.client.itemlist;

import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.projectlist.ProjectListPlace;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ItemListPlace extends ApplicationPlace {

	@Prefix("items")
	public static class Tokenizer implements PlaceTokenizer<ItemListPlace> {

		@Override
		public ItemListPlace getPlace(String token) {
			int lastSlash = token.lastIndexOf('!');
			if (lastSlash == -1) {
				return build(token, null);
			}

			return build(token.substring(0, lastSlash), token.substring(lastSlash + 1));
		}

		@Override
		public String getToken(ItemListPlace place) {
			String parentKey = place.getParentKey();
			String projectKey = place.getProjectKey();

			if (parentKey == null)
				return projectKey;
			return projectKey + "!" + parentKey;
		}

	}

	final String parentKey;
	final String projectKey;

	public ItemListPlace(String projectKey, String parentKey) {
		this.projectKey = projectKey;
		this.parentKey = parentKey;
	}

	public String getParentKey() {
		return parentKey;
	}

	@Override
	public ApplicationPlace getParent() {
		return ProjectListPlace.build();
	}

	@Override
	public String getLabel() {
		return "Project " + parentKey;
	}

	public static ItemListPlace build(String projectKey, String parentKey) {
		return new ItemListPlace(projectKey, parentKey);
	}

	public String getProjectKey() {
		return projectKey;
	}
}
