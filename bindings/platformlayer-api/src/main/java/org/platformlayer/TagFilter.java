package org.platformlayer;

import java.util.Arrays;
import java.util.List;

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
			if (!tag.getKey().equals(requiredTag.getKey())) {
				continue;
			}
			if (!tag.getValue().equals(requiredTag.getValue())) {
				continue;
			}

			return true;
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

	public Tag getRequiredTag() {
		return requiredTag;
	}

	@Override
	public String toString() {
		return "TagFilter [requiredTag=" + requiredTag + "]";
	}

	@Override
	public List<Tag> getRequiredTags() {
		return Arrays.asList(requiredTag);
	}

}
