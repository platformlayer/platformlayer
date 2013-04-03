package org.platformlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

	@Override
	public String toString() {
		return "AndFilter [filters=" + Arrays.toString(filters) + "]";
	}

	@Override
	public List<Tag> getRequiredTags() {
		Set<Tag> tags = Sets.newHashSet();

		for (Filter filter : filters) {
			tags.addAll(filter.getRequiredTags());
		}

		return Lists.newArrayList(tags);
	}

}
