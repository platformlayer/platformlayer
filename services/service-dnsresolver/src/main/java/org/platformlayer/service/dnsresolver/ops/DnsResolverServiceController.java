package org.platformlayer.service.dnsresolver.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.Strings;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.CollectdCollector;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.dnsresolver.model.DnsResolverService;

public class DnsResolverServiceController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(DnsResolverServiceController.class);

    @Inject
    ImageFactory imageFactory;

    @Handler
    public void doOperation() {
    }

    @Override
    protected void addChildren() throws OpsException {
        DnsResolverService model = OpsContext.get().getInstance(DnsResolverService.class);
        if (Strings.isEmpty(model.dnsName)) {
            throw new IllegalArgumentException("dnsName must be specified");
        }

        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        addChild(instance);

        instance.addChild(PackageDependency.build("bind9"));
        instance.addChild(ManagedService.build("bind9"));

        instance.addChild(CollectdCollector.build());

        // Debian bind9 sets up a recursive resolver by default :-)

        // TODO: Monit

        // TODO: Configure /etc/resolv.conf on servers
        // TODO: Refresh all our servers so that they use this resolver??

    }
}
