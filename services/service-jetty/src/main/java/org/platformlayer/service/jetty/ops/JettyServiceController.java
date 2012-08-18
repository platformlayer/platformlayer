package org.platformlayer.service.jetty.ops;

import java.util.List;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.HasPorts;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.jetty.model.JettyService;

import com.google.common.collect.Lists;

public class JettyServiceController extends OpsTreeBase implements HasPorts {
	static final Logger log = Logger.getLogger(JettyServiceController.class);

	public static final int PORT = 8080;

	@Bound
	JettyService model;

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {
		String dnsName = model.dnsName;

		List<Integer> ports = getPorts();

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.publicPorts.addAll(ports);
			// vm.hostPolicy.configureCluster(template.getPlacementKey());

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		vm.addChild(JettyInstall.class);

		vm.addChild(JettyInstance.class);

		for (int port : ports) {
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			vm.addChild(endpoint);
		}

		vm.addChild(MetricsInstance.class);
	}

	@Override
	public List<Integer> getPorts() {
		List<Integer> ports = Lists.newArrayList();
		ports.add(JettyServiceController.PORT);
		return ports;
	}

}
