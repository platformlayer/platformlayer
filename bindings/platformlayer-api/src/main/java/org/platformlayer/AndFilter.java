package org.platformlayer;

import org.platformlayer.core.model.ItemBase;

public class AndFilter extends Filter {

	private final Filter[] filters;

	public AndFilter(Filter[] filters) {
		this.filters = filters;
	}

	@Override
	public <T extends ItemBase> boolean matchesItem(T item) {
		for (Filter filter : filters) {
			if (!filter.matchesItem(item)) {
				return false;
			}
		}
		return true;
	}
}
