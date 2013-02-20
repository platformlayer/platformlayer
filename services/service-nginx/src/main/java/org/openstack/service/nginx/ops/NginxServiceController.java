package org.openstack.service.nginx.ops;

import java.io.IOException;

import org.openstack.service.nginx.model.NginxService;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.service.ManagedService;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NginxServiceController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(NginxServiceController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		NginxService model = OpsContext.get().getInstance(NginxService.class);

		InstanceBuilder instance = InstanceBuilder.build(model.dnsName, this, model.getTags());
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

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			instance.addChild(endpoint);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			endpoint.publicPort = 443;
			endpoint.backendPort = 443;
			endpoint.dnsName = model.dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			instance.addChild(endpoint);
		}

		addChild(injected(NginxBackendConfiguration.class));
		addChild(injected(NginxSites.class));
	}

}
