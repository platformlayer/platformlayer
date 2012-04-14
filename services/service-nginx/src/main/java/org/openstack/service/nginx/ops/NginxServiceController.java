package org.openstack.service.nginx.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openstack.service.nginx.model.NginxService;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;

public class NginxServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(NginxServiceController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		NginxService model = OpsContext.get().getInstance(NginxService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName,
				DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		instance.hostPolicy.allowRunInContainer = true;
		instance.publicPorts.add(80);
		instance.publicPorts.add(443);
		addChild(instance);

		instance.addChild(PackageDependency.build("nginx"));

		instance.addChild(NginxServerBootstrap.build());

		// Can't restart collectd
		// instance.addChild(CollectdCollector.build());

		instance.addChild(ManagedService.build("nginx"));

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			endpoint.publicPort = 80;
			endpoint.backendPort = 80;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			endpoint.publicPort = 443;
			endpoint.backendPort = 443;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			instance.addChild(endpoint);
		}

		addChild(injected(NginxBackendConfiguration.class));
		addChild(injected(NginxSites.class));
	}

}
