package org.platformlayer;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

public class TagFilter extends Filter {
	final Tag requiredTag;

	public TagFilter(Tag requiredTag) {
		this.requiredTag = requiredTag;
	}

	public static Filter byTag(Tag requiredTag) {
		TagFilter filter = new TagFilter(requiredTag);
		return filter;
	}

	public static Filter byParent(ItemBase parent) {
		return byParent(parent.getKey());
	}

	public static Filter byParent(PlatformLayerKey parentKey) {
		return byTag(Tag.buildParentTag(parentKey));
	}

	public boolean matchesTags(Iterable<Tag> tags) {
		if (requiredTag == null) {
			throw new IllegalStateException();
		}

		for (Tag tag : tags) {
			if (tag.equals(requiredTag)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean matchesItem(ItemBase item) {
		if (requiredTag == null) {
			throw new IllegalStateException();
		}

		Tags tags = item.getTags();
		return matchesTags(tags);
	}

}
