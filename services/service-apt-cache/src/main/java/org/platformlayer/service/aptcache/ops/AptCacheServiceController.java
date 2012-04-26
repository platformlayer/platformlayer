package org.platformlayer.service.aptcache.ops;

import java.io.File;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.aptcache.model.AptCacheService;

public class AptCacheServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(AptCacheServiceController.class);
	public static final int PORT = 3128;

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		AptCacheService model = OpsContext.get().getInstance(AptCacheService.class);

		// TODO: Create endpoint with default port; maybe default to closed?
		// model.dnsName

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		instance.hostPolicy.allowRunInContainer = true;
		addChild(instance);

		instance.addChild(PackageDependency.build("squid3"));

		instance.addChild(SimpleFile.build(getClass(), new File("/etc/squid3/squid.conf")));

		instance.addChild(ManagedService.build("squid3"));

		// instance.addChild(CollectdCollector.build());

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			endpoint.publicPort = PORT;
			endpoint.backendPort = PORT;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}
	}
}
