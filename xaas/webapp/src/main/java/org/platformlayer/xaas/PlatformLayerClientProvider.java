package org.platformlayer.xaas;

import javax.inject.Inject;

import org.platformlayer.PlatformLayerClient;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

import com.google.inject.Provider;

public class PlatformLayerClientProvider implements Provider<PlatformLayerClient> {
    @Inject
    OpsContext opsContext;

    @Override
    public PlatformLayerClient get() {
        try {
            return opsContext.getUserInfo().getPlatformLayerClient();
        } catch (OpsException e) {
            throw new IllegalStateException("Error building conductor client", e);
        }
    }
}
