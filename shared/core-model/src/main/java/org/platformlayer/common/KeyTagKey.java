package org.platformlayer.common;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;

public class KeyTagKey extends TagKey<PlatformLayerKey> {
	public KeyTagKey(String key) {
		super(key, null);
	}

	@Override
	protected PlatformLayerKey toT(String s) {
		return PlatformLayerKey.parse(s);
	}

	public Tag build(PlatformLayerKey t) {
		return Tag.build(key, t.getUrl());
	}

}