package org.platformlayer;

import org.json.JSONObject;
import org.platformlayer.common.UntypedItem;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tags;

public class UntypedItemJson implements UntypedItem {
	private final JSONObject json;

	private UntypedItemJson(JSONObject json) {
		this.json = json;
	}

	@Override
	public PlatformLayerKey getKey() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Tags getTags() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ManagedItemState getState() {
		throw new UnsupportedOperationException();
	}

	public static UntypedItemJson build(String data) {
		JSONObject json;

		try {
			json = new JSONObject(data);
		} catch (Exception e) {
			throw new IllegalArgumentException("Error parsing JSON", e);
		}

		return new UntypedItemJson(json);
	}

}
