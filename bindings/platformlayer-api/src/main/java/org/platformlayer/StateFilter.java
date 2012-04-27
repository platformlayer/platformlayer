package org.platformlayer;

import java.util.EnumSet;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;

public class StateFilter extends Filter {

	private final EnumSet<ManagedItemState> allowStates;

	public StateFilter(EnumSet<ManagedItemState> allowStates) {
		this.allowStates = allowStates;
	}

	@Override
	public <T extends ItemBase> boolean matchesItem(T item) {
		ManagedItemState itemState = item.getState();

		if (itemState == null) {
			throw new IllegalStateException();
		}

		return allowStates.contains(itemState);
	}

}
