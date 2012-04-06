package org.platformlayer.service.nexus.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.service.jetty.ops.JettyInstance;
import org.platformlayer.service.nexus.model.NexusService;

public class NexusServiceController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(NexusServiceController.class);

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        NexusService model = OpsContext.get().getInstance(NexusService.class);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        instance.minimumMemoryMb = 2048;
        addChild(instance);

        instance.addChild(NexusBootstrap.build());

        JettyInstance jetty = instance.addChild(JettyInstance.build());
        jetty.addApp(NexusApp.build());

        instance.addChild(CollectdCollector.build());
    }

}
