package org.platformlayer.gwt.client.item;

import org.platformlayer.gwt.client.itemlist.ItemListPlace;
import org.platformlayer.gwt.client.places.ApplicationPlace;

public class ItemPlace extends ApplicationPlace {
	public ItemPlace(ItemListPlace parent, String itemKey) {
		super(parent, itemKey);
	}

	@Override
	public String getLabel() {
		return "Item " + getPathToken();
	}

	@Override
	public ApplicationPlace getChild(String pathToken) {
		return null;
	}

	public String getItemPath() {
		return getPathToken();
	}

}
