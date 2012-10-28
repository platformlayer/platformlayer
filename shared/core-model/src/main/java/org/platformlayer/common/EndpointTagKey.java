package org.platformlayer.common;

import org.platformlayer.core.model.EndpointInfo;
import org.platformlayer.core.model.Tag;

public class EndpointTagKey extends TagKey<EndpointInfo> {
	public EndpointTagKey(String key) {
		super(key, null);
	}

	@Override
	protected EndpointInfo toT(String s) {
		return EndpointInfo.parseTagValue(s);
	}

	public Tag build(EndpointInfo t) {
		return Tag.build(key, t.getTagValue());
	}
}