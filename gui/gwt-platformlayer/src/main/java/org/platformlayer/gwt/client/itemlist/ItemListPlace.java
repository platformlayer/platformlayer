package org.platformlayer.gwt.client.itemlist;

import org.platformlayer.gwt.client.item.ItemPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;
import org.platformlayer.gwt.client.project.ProjectPlace;

public class ItemListPlace extends ApplicationPlace {

	public static final String KEY_ROOTS = "roots";

	private ItemListPlace(ProjectPlace project, String key) {
		super(project, key);
	}

	@Override
	public ProjectPlace getParent() {
		return (ProjectPlace) super.getParent();
	}

	@Override
	public String getLabel() {
		return "Items";
	}

	public String getProjectKey() {
		return getProject().getProjectKey();
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return getItem(pathToken);
	}

	public static ItemListPlace buildRoots(ProjectPlace parent) {
		return new ItemListPlace(parent, KEY_ROOTS);
	}

	public ItemPlace getItem(String path) {
		return new ItemPlace(this, path);
	}

}
