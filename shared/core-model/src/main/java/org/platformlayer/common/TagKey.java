package org.platformlayer.common;

import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.collect.Lists;

public abstract class TagKey<T> {
	final String key;

	final T defaultValue;

	public TagKey(String key, T defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public Iterable<Tag> filter(Tags tags) {
		List<Tag> matches = Lists.newArrayList();
		for (Tag tag : tags) {
			if (key.equals(tag.key)) {
				matches.add(tag);
			}
		}
		return matches;
	}

	public T findUnique(Tags tags) {
		String s = tags.findUnique(key);
		if (s == null) {
			return defaultValue;
		}
		return toT(s);
	}

	public T findUnique(IsItem tags) {
		return findUnique(tags.getTags());
	}

	public T findFirst(Tags tags) {
		String s = tags.findFirst(key);
		if (s == null) {
			return defaultValue;
		}
		return toT(s);
	}

	public T findFirst(IsItem item) {
		return findFirst(item.getTags());
	}

	// public Tag findUniqueTag(HasTags tags) {
	// return tags.findUniqueTag(key);
	// }
	//
	// public Tag findUniqueTag(ItemBase item) {
	// return findUniqueTag(item.getTags());
	// }

	public List<T> find(Tags tags) {
		List<T> ret = Lists.newArrayList();
		for (String s : tags.findAll(key)) {
			ret.add(toT(s));
		}
		return ret;
	}

	public List<T> find(IsItem item) {
		return find(item.getTags());
	}

	protected abstract T toT(String s);

	// public boolean isTag(Tag tag) {
	// return key.equals(tag.getKey());
	// }
}