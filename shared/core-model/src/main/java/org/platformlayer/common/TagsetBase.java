package org.platformlayer.common;

import java.util.Iterator;
import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public abstract class TagsetBase implements Iterable<IsTag>, Tagset {
	java.util.logging.Logger log = java.util.logging.Logger.getLogger(TagsetBase.class.getName());

	public void add(String key, String value) {
		Tag tag = Tag.build(key, value);
		getTags().add(tag);
	}

	public abstract List<IsTag> getTags();

	@Override
	public void add(IsTag tag) {
		getTags().add(tag);
	}

	public boolean isEmpty() {
		return getTags().isEmpty();
	}

	@Override
	public Iterator<IsTag> iterator() {
		return getTags().iterator();
	}

	@Override
	public List<String> findAll(String key) {
		List<String> matches = Lists.newArrayList();
		for (IsTag tag : getTags()) {
			if (key.equals(tag.getKey())) {
				matches.add(tag.getValue());
			}
		}
		return matches;
	}

	public List<IsTag> findTags(String key) {
		List<IsTag> matches = Lists.newArrayList();
		for (IsTag tag : getTags()) {
			if (key.equals(tag.getKey())) {
				matches.add(tag);
			}
		}
		return matches;
	}

	public boolean remove(IsTag removeTag) {
		return getTags().remove(removeTag);
	}

	@Override
	public String findUnique(String key) {
		IsTag tag = findUniqueTag(key);
		if (tag == null) {
			return null;
		}
		return tag.getValue();
	}

	public IsTag findUniqueTag(String key) {
		List<IsTag> matches = findTags(key);
		if (matches.size() == 0) {
			return null;
		}
		if (matches.size() > 1) {
			// throw new IllegalStateException("Found duplicate tags for key: " + key);
			log.warning("Found duplicate tags for key: " + key + " matches=" + Joiner.on(',').join(matches));
		}
		return matches.get(0);
	}

	public boolean hasTag(String key, String value) {
		return hasTag(Tag.build(key, value));
	}

	public boolean hasTag(IsTag find) {
		for (IsTag tag : getTags()) {
			if (Tags.equals(tag, find)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addAll(Iterable<? extends IsTag> tags) {
		if (tags != null) {
			for (IsTag tag : tags) {
				add(tag);
			}
		}
	}

	@Override
	public String toString() {
		return "Tags [tags=" + getTags() + "]";
	}

	@Override
	public String findFirst(String key) {
		for (IsTag tag : getTags()) {
			if (key.equals(tag.getKey())) {
				return tag.getValue();
			}
		}
		return null;
	}

	public static Tags build() {
		return new Tags();
	}

	public static Tags build(List<IsTag> tags) {
		Tags o = new Tags();
		o.tags = tags;
		return o;
	}

	public static boolean equals(IsTag a, IsTag b) {
		if (a == null || b == null) {
			return a == b;
		}

		if (!a.getKey().equals(b.getKey())) {
			return false;
		}
		if (!a.getValue().equals(b.getValue())) {
			return false;
		}

		return true;
	}
}
