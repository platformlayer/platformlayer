package org.platformlayer.service.zookeeper.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.vpn.IpsecForPort;
import org.platformlayer.ops.vpn.IpsecInstall;
import org.platformlayer.ops.vpn.IpsecPresharedKey;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;

public class ZookeeperServerController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(ZookeeperServerController.class);

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		ZookeeperServer model = OpsContext.get().getInstance(ZookeeperServer.class);

		int port = ZookeeperConstants.ZK_PUBLIC_PORT;

		// A per-instance name (for convenience)
		String dnsName = ZookeeperUtils.buildDnsName(model);

		InstanceBuilder vm = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		vm.publicPorts.add(port);
		vm.publicPorts.add(ZookeeperConstants.ZK_SYSTEM_PORT_1);
		vm.publicPorts.add(ZookeeperConstants.ZK_SYSTEM_PORT_2);

		vm.addChild(IpsecInstall.class);

		ZookeeperInstanceModel template = injected(ZookeeperInstanceModel.class);

		{
			IpsecPresharedKey ipsec = vm.addChild(IpsecPresharedKey.class);
			ipsec.id = IpsecPresharedKey.SHAREDKEY_USER_FQDN;
			ipsec.secret = template.getIpsecSecret();
		}

		// The system ports are used for communication between nodes,
		// so need to be opened early
		for (int systemPort : ZookeeperConstants.SYSTEM_PORTS) {
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = systemPort;
			endpoint.backendPort = systemPort;
			endpoint.dnsName = dnsName;

			// We expect this to be used by IPv6 capable client
			endpoint.transport = Transport.Ipv6;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			vm.addChild(endpoint);

			{
				IpsecForPort ipsecForPort = vm.addChild(IpsecForPort.class);
				ipsecForPort.port = systemPort;
			}
		}

		vm.hostPolicy.allowRunInContainer = true;

		// TODO: This needs to be configurable...
		vm.minimumMemoryMb = 2048;

		addChild(vm);

		{
			ZookeeperInstall install = injected(ZookeeperInstall.class);
			vm.addChild(install);
		}

		{
			ZookeeperInstance service = injected(ZookeeperInstance.class);
			vm.addChild(service);
		}

		{
			PublicEndpoint endpoint = injected(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			// We expect this to be used by IPv6 capable client
			endpoint.transport = Transport.Ipv6;

			endpoint.tagItem = OpsSystem.toKey(model);
			endpoint.parentItem = OpsSystem.toKey(model);

			vm.addChild(endpoint);

			{
				IpsecForPort ipsecForPort = vm.addChild(IpsecForPort.class);
				ipsecForPort.port = port;
			}
		}

		vm.addChild(ZookeeperStatusChecker.class);

		// TODO: Establish round-robin style DNS on clusterDnsName
		// TODO: Is some form of geo-direction possible?
	}
}
