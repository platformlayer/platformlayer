package org.platformlayer.service.dns.ops;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.dns.model.DnsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DnsServerController extends OpsTreeBase {
	private static final Logger log = LoggerFactory.getLogger(DnsServerController.class);

	@Handler
	public void handler() {
	}

	@Bound
	DnsServer model;

	@Override
	protected void addChildren() throws OpsException {
		InstanceBuilder vm = InstanceBuilder.build(model.dnsName, this);

		// TODO: Do we need a DnsCluster concept?
		// For now, we fake it
		PlatformLayerKey key = model.getKey();
		String groupId = key.withId(new ManagedItemId("primary")).getUrl();
		groupId = groupId.replace("/dnsServer/", "/dnsCluster/");
		vm.hostPolicy.configureSpread(groupId);

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

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

			vm.addChild(endpoint);
		}

		// TODO: Refresh other DNS servers so they also point to this server
	}
}
