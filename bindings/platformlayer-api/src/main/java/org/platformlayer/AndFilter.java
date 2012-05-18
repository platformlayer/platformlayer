package org.platformlayer;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.ItemBase;

public class AndFilter extends Filter {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AndFilter.class);

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
