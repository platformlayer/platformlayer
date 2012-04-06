package org.platformlayer.ops;

import javax.inject.Inject;
import javax.inject.Provider;

public class CloudContextProvider implements Provider<CloudContext>, com.google.inject.Provider<CloudContext> {
    @Inject
    CloudContextRegistry cloudContextRegistry;

    @Override
    public CloudContext get() {
        UserInfo userInfo = OpsContext.get().getUserInfo();
        try {
            return cloudContextRegistry.getCloudContext(userInfo);
        } catch (OpsException e) {
            throw new IllegalStateException("Error getting cloud context", e);
        }
    }

}
