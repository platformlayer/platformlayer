package org.platformlayer.service.redis.ops;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.redis.model.RedisServer;

public class RedisServerController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(RedisServerController.class);

    public static final int PORT = 6379;

    @Handler
    public void handler() throws OpsException, IOException {
    }

    @Override
    protected void addChildren() throws OpsException {
        RedisServer model = OpsContext.get().getInstance(RedisServer.class);

        InstanceBuilder vm;

        {
            vm = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));

            // TODO: Memory _really_ needs to be configurable here!
            vm.publicPorts.add(PORT);

            vm.minimumMemoryMb = 1024;

            vm.hostPolicy.allowRunInContainer = true;
            addChild(vm);
        }

        vm.addChild(PackageDependency.build("redis-server"));

        RedisTemplateModel template = injected(RedisTemplateModel.class);

        vm.addChild(TemplatedFile.build(template, new File("/etc/redis/redis.conf")).setFileMode("444"));

        // Collectd not restarting correctly (doesn't appear to be hostname problems??)
        // instance.addChild(CollectdCollector.build());

        {
            PublicEndpoint endpoint = injected(PublicEndpoint.class);
            // endpoint.network = null;
            endpoint.publicPort = PORT;
            endpoint.backendPort = PORT;
            endpoint.dnsName = model.dnsName;

            endpoint.tagItem = model.getKey();
            endpoint.parentItem = model.getKey();

            vm.addChild(endpoint);
        }

        vm.addChild(ManagedService.build("redis-server"));
    }
}
