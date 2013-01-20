package org.platformlayer.common;

import org.platformlayer.core.model.Tag;

public class StringTagKey extends TagKey<String> {
	public StringTagKey(String key) {
		super(key, null);
	}

	@Override
	protected String toT(String s) {
		return s;
	}

	public Tag build(String t) {
		return Tag.build(key, t);
	}

}