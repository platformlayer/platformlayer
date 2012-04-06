//package org.platformlayer.service.aptcache.ops;
//
//import java.io.IOException;
//
//import org.apache.log4j.Logger;
//import org.platformlayer.ops.Handler;
//import org.platformlayer.ops.OpsContext;
//import org.platformlayer.ops.OpsException;
//import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
//import org.platformlayer.ops.instances.InstanceBuilder;
//import org.platformlayer.ops.metrics.collectd.CollectdCollector;
//import org.platformlayer.ops.metrics.collectd.ManagedService;
//import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
//import org.platformlayer.ops.packages.PackageDependency;
//import org.platformlayer.service.aptcache.model.AptCacheService;
//
//public class AptCacherNgAptCacheServiceController extends OpsTreeBase {
//    static final Logger log = Logger.getLogger(AptCacherNgAptCacheServiceController.class);
//
//    @Handler
//    public void doOperation() throws OpsException, IOException {
//    }
//
//    @Override
//    protected void addChildren() throws OpsException {
//        AptCacheService model = OpsContext.get().getInstance(AptCacheService.class);
//
//        InstanceBuilder instance = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
//        addChild(instance);
//
//        instance.addChild(PackageDependency.build("apt-cacher-ng"));
//        instance.addChild(ManagedService.build("apt-cacher-ng"));
//
//        instance.addChild(CollectdCollector.build());
//    }
//
// }
