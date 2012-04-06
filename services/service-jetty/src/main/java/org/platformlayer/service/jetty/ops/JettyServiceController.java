package org.platformlayer.service.jetty.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.service.jetty.model.JettyService;

public class JettyServiceController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(JettyServiceController.class);

    @Handler
    public void doOperation() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        JettyService model = OpsContext.get().getInstance(JettyService.class);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        instance.minimumMemoryMb = 2048;
        addChild(instance);

        instance.addChild(JettyInstance.build());

        instance.addChild(CollectdCollector.build());
    }
}
