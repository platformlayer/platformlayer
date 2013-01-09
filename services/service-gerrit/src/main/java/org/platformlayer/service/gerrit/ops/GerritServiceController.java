package org.platformlayer.service.gerrit.ops;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.metrics.MetricsInstance;
import org.platformlayer.ops.networks.HasPorts;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.gerrit.model.GerritService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class GerritServiceController extends OpsTreeBase implements HasPorts {

	private static final Logger log = LoggerFactory.getLogger(GerritServiceController.class);

	@Bound
	public GerritService model;

	@Inject
	GerritTemplate template;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		// GerritInstanceModel template = injected(GerritInstanceModel.class);
		// GerritService model = template.getModel();

		String dnsName = model.dnsName;

		List<Integer> ports = getPorts();

		InstanceBuilder vm;
		{
			vm = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
			vm.publicPorts.addAll(ports);
			vm.hostPolicy.configureCluster(template.getPlacementKey());

			// TODO: This needs to be configurable (?)
			vm.minimumMemoryMb = 2048;

			addChild(vm);
		}

		{
			GerritInstall install = vm.addChild(GerritInstall.class);
		}

		// Bootstrap depends on configuration file
		vm.addChild(ManagedDirectory.build(template.getDataDir(), "700").setOwner(template.getUser())
				.setGroup(template.getGroup()));
		vm.addChild(ManagedDirectory.build(new File(template.getDataDir(), "etc"), "700").setOwner(template.getUser())
				.setGroup(template.getGroup()));
		vm.addChild(GerritConfigurationFile.class);

		vm.addChild(GerritBootstrap.class);

		vm.addChild(GerritInstance.class);

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
		ports.add(template.getWebPort());
		ports.add(template.getSshdPort());
		return ports;
	}
}
