package org.platformlayer.ids;

import org.platformlayer.model.StringWrapper;

import com.google.common.base.Strings;

public class FederationKey extends StringWrapper {

	public static final FederationKey LOCAL_FEDERATION_KEY = new FederationKey("");

	private FederationKey(String key) {
		super(key);

		if (key == null) {
			// Should be empty string, for LOCAL_FEDERATION_KEY
			throw new IllegalArgumentException();
		}
	}

	public static FederationKey build(String server) {
		if (Strings.isNullOrEmpty(server)) {
			return LOCAL_FEDERATION_KEY;
		}
		return new FederationKey(server);
	}

}
