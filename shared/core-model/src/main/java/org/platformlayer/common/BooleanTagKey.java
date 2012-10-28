package org.platformlayer.common;

import org.platformlayer.core.model.Tag;

public class BooleanTagKey extends TagKey<Boolean> {
	public BooleanTagKey(String key, boolean defaultValue) {
		super(key, defaultValue);
	}

	public BooleanTagKey(String key) {
		super(key, null);
	}

	@Override
	protected Boolean toT(String s) {
		return Boolean.valueOf(s);
	}

	public Tag build(Boolean v) {
		String s = v.toString();
		return Tag.build(key, s);
	}
}