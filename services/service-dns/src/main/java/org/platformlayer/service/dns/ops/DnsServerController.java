package org.platformlayer.service.dns.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.firewall.FirewallRecord.Protocol;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.dns.model.DnsServer;

public class DnsServerController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(DnsServerController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		DnsServer model = OpsContext.get().getInstance(DnsServer.class);

		InstanceBuilder vm = InstanceBuilder.build(model.dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		vm.addTagToManaged = true;
		vm.publicPorts.add(53);

		// vm.minimumMemoryMb = 512;

		addChild(vm);

		vm.addChild(DnsServerInstall.class);
		vm.addChild(DnsServerInstance.class);

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = 53;
			endpoint.backendPort = 53;
			endpoint.dnsName = model.dnsName;
			endpoint.protocol = Protocol.Udp;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			vm.addChild(endpoint);
		}

		// TODO: Refresh other DNS servers so they also point to this server
	}
}
