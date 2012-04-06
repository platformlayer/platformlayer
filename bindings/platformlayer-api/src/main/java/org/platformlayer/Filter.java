package org.platformlayer;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

public class Filter {
    public static final Filter EMPTY = null;

    Tag requiredTag;

    public static Filter byTag(Tag requiredTag) {
        Filter filter = new Filter();
        filter.requiredTag = requiredTag;
        return filter;
    }

    public boolean matchesTags(Iterable<Tag> tags) {
        if (requiredTag == null)
            throw new IllegalStateException();

        for (Tag tag : tags) {
            if (tag.equals(requiredTag)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(Object item) {
        if (requiredTag == null)
            throw new IllegalStateException();

        if (item instanceof ItemBase) {
            Tags tags = ((ItemBase) item).getTags();
            return matchesTags(tags);
        }

        throw new IllegalArgumentException("Custom items not yet supported with filter");
    }

}
