package org.platformlayer.service.memcache.ops;

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
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.memcache.model.MemcacheServer;

public class MemcacheServerController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(MemcacheServerController.class);

	public static final int MEMCACHE_PORT = 11211;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		MemcacheServer model = OpsContext.get().getInstance(MemcacheServer.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));

		// TODO: Memory _really_ needs to be configurable here!
		instance.publicPorts.add(MEMCACHE_PORT);

		instance.minimumMemoryMb = 1024;

		instance.hostPolicy.allowRunInContainer = true;
		addChild(instance);

		instance.addChild(PackageDependency.build("memcached"));

		MemcacheTemplateModel template = injected(MemcacheTemplateModel.class);

		instance.addChild(TemplatedFile.build(template, new File("/etc/memcached.conf")));

		// Collectd not restarting correctly (doesn't appear to be hostname problems??)
		// instance.addChild(CollectdCollector.build());

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = MEMCACHE_PORT;
			endpoint.backendPort = MEMCACHE_PORT;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}

		instance.addChild(ManagedService.build("memcached"));
	}
}
