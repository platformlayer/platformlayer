package org.platformlayer;

import java.util.List;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;

public abstract class Filter {
	public static final Filter EMPTY = null;

	public boolean matches(Object item) {
		if (item instanceof ItemBase) {
			return matchesItem((ItemBase) item);
		}

		throw new IllegalArgumentException("Custom items not yet supported with filter: " + this);
	}

	public abstract <T extends ItemBase> boolean matchesItem(T item);

	public static Filter and(Filter... filters) {
		return new AndFilter(filters);
	}

	public abstract List<Tag> getRequiredTags();
}
