package org.platformlayer.ops;

import javax.inject.Inject;
import javax.inject.Provider;

public class CloudContextProvider implements Provider<CloudContext>, com.google.inject.Provider<CloudContext> {
	@Inject
	CloudContextRegistry cloudContextRegistry;

	@Override
	public CloudContext get() {
		OpsContext opsContext = OpsContext.get();
		if (opsContext == null) {
			return null;
		}

		UserInfo userInfo = opsContext.getUserInfo();
		try {
			return cloudContextRegistry.getCloudContext(userInfo);
		} catch (OpsException e) {
			throw new IllegalStateException("Error getting cloud context", e);
		}
	}

}
