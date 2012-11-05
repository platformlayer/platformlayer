package org.platformlayer.common;

import org.platformlayer.core.model.Tag;

public class FloatTagKey extends TagKey<Float> {
	public FloatTagKey(String key, float defaultValue) {
		super(key, defaultValue);
	}

	public FloatTagKey(String key) {
		super(key, null);
	}

	@Override
	protected Float toT(String s) {
		return Float.parseFloat(s);
	}

	public Tag build(Float v) {
		String s = v.toString();
		return Tag.build(key, s);
	}
}