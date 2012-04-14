package org.platformlayer;

import java.util.List;

import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;

import com.google.common.collect.Lists;

public class PlatformLayerUtils {
	public static List<String> findEndpoints(Tags tags) {
		List<String> dest = Lists.newArrayList();

		String v;

		v = tags.findUnique("endpoint");
		if (v != null) {
			dest.add(v);
		}

		v = tags.findUnique(Tag.NETWORK_ADDRESS);
		if (v != null) {
			dest.add(v);
		}

		for (String endpoint : tags.find(Tag.PUBLIC_ENDPOINT)) {
			dest.add(endpoint);
		}

		return dest;
	}
}
