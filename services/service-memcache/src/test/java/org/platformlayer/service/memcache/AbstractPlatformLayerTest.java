package org.platformlayer.service.memcache;

import java.io.IOException;

import org.openstack.client.OpenstackException;
import org.openstack.client.utils.RandomUtil;
import org.platformlayer.TypedItemMapper;
import org.platformlayer.TypedPlatformLayerClient;
import org.platformlayer.ops.OpsException;

public abstract class AbstractPlatformLayerTest {
    protected PlatformLayerTestContext context;
    protected RandomUtil random = new RandomUtil();

    protected abstract TypedItemMapper getTypedItemMapper();

    protected void reset() {
        context = null;
    }

    public TypedPlatformLayerClient getTypedClient() throws IOException, OpsException {
        return getContext().getTypedClient();
    }

    private PlatformLayerTestContext getContext() {
        if (context == null) {
            context = PlatformLayerTestContext.buildFromProperties(getTypedItemMapper());
        }
        return context;
    }

}
