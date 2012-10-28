package org.platformlayer;

import org.platformlayer.common.IsTag;
import org.platformlayer.common.Tagset;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

public class TagFilter extends Filter {
	final IsTag requiredTag;

	public TagFilter(IsTag requiredTag) {
		this.requiredTag = requiredTag;
	}

	public static Filter byTag(IsTag requiredTag) {
		TagFilter filter = new TagFilter(requiredTag);
		return filter;
	}

	public static Filter byParent(ItemBase parent) {
		return byParent(parent.getKey());
	}

	public static Filter byParent(PlatformLayerKey parentKey) {
		return byTag(Tag.buildParentTag(parentKey));
	}

	public boolean matchesTags(Iterable<IsTag> tags) {
		if (requiredTag == null) {
			throw new IllegalStateException();
		}

		for (IsTag tag : tags) {
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

		Tagset tags = item.getTags();
		return matchesTags(tags);
	}

}
