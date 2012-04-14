package org.platformlayer.ids;

import org.platformlayer.model.StringWrapper;

public class FederationKey extends StringWrapper {

	public static final FederationKey LOCAL_FEDERATION_KEY = new FederationKey("");

	public FederationKey(String key) {
		super(key);
	}

}
