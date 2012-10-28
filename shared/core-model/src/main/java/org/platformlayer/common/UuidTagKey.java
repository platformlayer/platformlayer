package org.platformlayer.common;

import org.platformlayer.core.model.Tag;

public class UuidTagKey extends TagKey<java.util.UUID> {
	public UuidTagKey(String key) {
		super(key, null);
	}

	@Override
	protected java.util.UUID toT(String s) {
		return java.util.UUID.fromString(s);
	}

	public Tag build(java.util.UUID t) {
		return Tag.build(key, t.toString());
	}

}
