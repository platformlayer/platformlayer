package org.platformlayer;

import org.platformlayer.core.model.ItemBase;

public abstract class Filter {
	public static final Filter EMPTY = null;

	public boolean matches(Object item) {
		if (item instanceof ItemBase) {
			return matchesItem((ItemBase) item);
		}

		throw new IllegalArgumentException("Custom items not yet supported with filter: " + this);
	}

	public abstract <T extends ItemBase> boolean matchesItem(T item);
}
