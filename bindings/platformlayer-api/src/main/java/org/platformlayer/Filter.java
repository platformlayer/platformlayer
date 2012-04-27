package org.platformlayer;

import java.util.EnumSet;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.Tag;

public abstract class Filter {
	public static final Filter EMPTY = null;

	public static Filter byTag(Tag requiredTag) {
		TagFilter filter = new TagFilter(requiredTag);
		return filter;
	}

	public static Filter byParent(ItemBase item) {
		return byTag(Tag.buildParentTag(item.getKey()));
	}

	public static Filter excludeStates(ManagedItemState... states) {
		EnumSet<ManagedItemState> allowStates = EnumSet.allOf(ManagedItemState.class);
		for (ManagedItemState state : states) {
			allowStates.remove(state);
		}

		return onlyStates(allowStates);
	}

	private static Filter onlyStates(EnumSet<ManagedItemState> allowStates) {
		return new StateFilter(allowStates);
	}

	public boolean matches(Object item) {
		if (item instanceof ItemBase) {
			return matchesItem((ItemBase) item);
		}

		throw new IllegalArgumentException("Custom items not yet supported with filter: " + this);
	}

	public abstract <T extends ItemBase> boolean matchesItem(T item);
}
