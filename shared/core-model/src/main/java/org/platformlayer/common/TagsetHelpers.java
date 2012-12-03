package org.platformlayer.common;

import java.util.List;

import org.platformlayer.core.model.Tag;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class TagsetHelpers {
	static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(TagsetHelpers.class.getName());

	public static boolean equals(Tag a, Tag b) {
		if (a == null || b == null) {
			return a == b;
		}

		if (!a.getKey().equals(b.getKey())) {
			return false;
		}
		if (!Objects.equal(a.getValue(), b.getValue())) {
			return false;
		}

		return true;
	}

	public static String findFirst(List<Tag> tags, String key) {
		for (Tag tag : tags) {
			if (key.equals(tag.getKey())) {
				return tag.getValue();
			}
		}
		return null;
	}

	public static Tag findUniqueTag(List<Tag> tags, String key) {
		List<Tag> matches = findTags(tags, key);
		if (matches.size() == 0) {
			return null;
		}
		if (matches.size() > 1) {
			// throw new IllegalStateException("Found duplicate tags for key: " + key);
			log.warning("Found duplicate tags for key: " + key + " matches=" + Joiner.on(',').join(matches));
		}
		return matches.get(0);
	}

	public static List<Tag> findTags(List<Tag> tags, String key) {
		List<Tag> matches = Lists.newArrayList();
		for (Tag tag : tags) {
			if (key.equals(tag.getKey())) {
				matches.add(tag);
			}
		}
		return matches;
	}

	public static List<String> findAll(List<Tag> tags, String key) {
		List<String> matches = Lists.newArrayList();
		for (Tag tag : tags) {
			if (key.equals(tag.getKey())) {
				matches.add(tag.getValue());
			}
		}
		return matches;
	}

	public static String findUnique(List<Tag> tags, String key) {
		Tag tag = findUniqueTag(tags, key);
		if (tag == null) {
			return null;
		}
		return tag.getValue();
	}

	public static boolean hasTag(List<Tag> tags, Tag find) {
		for (Tag tag : tags) {
			if (equals(tag, find)) {
				return true;
			}
		}
		return false;
	}
}
