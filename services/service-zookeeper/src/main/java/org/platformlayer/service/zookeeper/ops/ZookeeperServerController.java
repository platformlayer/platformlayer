package org.platformlayer.service.zookeeper.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.networks.IpsecHelpers;
import org.platformlayer.ops.networks.PublicEndpoint;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.ops.vpn.IpsecForPort;
import org.platformlayer.ops.vpn.IpsecInstall;
import org.platformlayer.ops.vpn.IpsecPresharedKey;
import org.platformlayer.service.zookeeper.model.ZookeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperServerController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(ZookeeperServerController.class);

	@Inject
	IpsecHelpers ipsec;

	@Bound
	ZookeeperServer model;

	@Bound
	ZookeeperInstanceModel template;

	@Handler
	public void doOperation() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		int port = ZookeeperConstants.ZK_PUBLIC_PORT;

		// A per-instance name (for convenience)
		String dnsName = ZookeeperUtils.buildDnsName(model);

		InstanceBuilder vm = InstanceBuilder.build(dnsName, this, model.getTags());
		// vm.publicPorts.add(port);
		// vm.publicPorts.add(ZookeeperConstants.ZK_SYSTEM_PORT_1);
		// vm.publicPorts.add(ZookeeperConstants.ZK_SYSTEM_PORT_2);

		vm.hostPolicy.configureSpread(template.getClusterGroupId());

		vm.addChild(IpsecInstall.class);

		{
			IpsecPresharedKey psk = vm.addChild(IpsecPresharedKey.class);
			psk.id = IpsecPresharedKey.SHAREDKEY_USER_FQDN;
			psk.secret = ipsec.getIpsecSecret();
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

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

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
			ZookeeperInstall install = vm.addChild(ZookeeperInstall.class);
		}

		{
			ZookeeperInstance service = vm.addChild(ZookeeperInstance.class);
		}

		{
			PublicEndpoint endpoint = vm.addChild(PublicEndpoint.class);
			// endpoint.network = null;
			endpoint.publicPort = port;
			endpoint.backendPort = port;
			endpoint.dnsName = dnsName;

			// We expect this to be used by IPv6 capable client
			endpoint.transport = Transport.Ipv6;

			endpoint.tagItem = model.getKey();
			endpoint.parentItem = model.getKey();

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
