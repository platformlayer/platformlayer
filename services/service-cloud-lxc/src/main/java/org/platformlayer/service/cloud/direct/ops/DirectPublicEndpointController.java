package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectPublicEndpoint;

public class DirectPublicEndpointController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(DirectPublicEndpointController.class);

    @Handler
    public void handler() throws OpsException, IOException {
    }

    @Inject
    PlatformLayerHelpers platformLayerClient;

    @Override
    protected void addChildren() throws OpsException {
        DirectPublicEndpoint model = OpsContext.get().getInstance(DirectPublicEndpoint.class);
        DirectInstance directInstance = platformLayerClient.getItem(DirectInstance.class, model.instance);

        {
            PublicPorts publicPorts = injected(PublicPorts.class);
            publicPorts.backendItem = directInstance;
            publicPorts.backendPort = model.backendPort;
            publicPorts.publicPort = model.publicPort;
            addChild(publicPorts);
        }
    }

}
