package org.platformlayer.service.vpn.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.service.vpn.model.VpnService;

public class VpnServiceController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(VpnServiceController.class);

    @Handler
    public void handler() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        VpnService model = OpsContext.get().getInstance(VpnService.class);

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        addChild(instance);

        instance.addChild(PackageDependency.build("strongswan"));

        instance.addChild(CollectdCollector.build());

        // instance.addChild(ManagedService.build("ipsec"));
    }

}
