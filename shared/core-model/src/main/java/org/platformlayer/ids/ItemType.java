package org.platformlayer.ids;

import org.platformlayer.model.StringWrapper;

public class ItemType extends StringWrapper {

	public ItemType(String key) {
		super(key);
	}

	public static ItemType wrap(String itemType) {
		if (itemType == null) {
			return null;
		}
		return new ItemType(itemType);
	}

}
