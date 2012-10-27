package org.platformlayer.common;

import java.util.List;

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

	public T findUnique(HasTags tags) {
		String s = tags.findUnique(key);
		if (s == null) {
			return defaultValue;
		}
		return toT(s);
	}

	public T findFirst(HasTags tags) {
		String s = tags.findFirst(key);
		if (s == null) {
			return defaultValue;
		}
		return toT(s);
	}

	// public Tag findUniqueTag(HasTags tags) {
	// return tags.findUniqueTag(key);
	// }
	//
	// public Tag findUniqueTag(ItemBase item) {
	// return findUniqueTag(item.getTags());
	// }

	public List<T> find(HasTags tags) {
		List<T> ret = Lists.newArrayList();
		for (String s : tags.findAll(key)) {
			ret.add(toT(s));
		}
		return ret;
	}

	protected abstract T toT(String s);

	// public boolean isTag(Tag tag) {
	// return key.equals(tag.getKey());
	// }
}