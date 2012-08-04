package org.platformlayer.ops.vpn;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.firewall.FirewallRecord.Direction;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.firewall.scripts.IptablesFilterEntry;
import org.platformlayer.ops.firewall.scripts.IptablesFilterPolicy;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.ManagedService;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;

public class IpsecInstall extends OpsTreeBase {

	@Handler
	public void handler() {
	}

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Override
	protected void addChildren() throws OpsException {
		addChild(PackageDependency.build("racoon"));

		addChild(SimpleFile.build(getClass(), new File("/etc/racoon/racoon.conf")));
		// addChild(SimpleFile.build(getClass(), new File("/etc/racoon/psk.txt")));
		addChild(SimpleFile.build(getClass(), new File("/etc/ipsec-tools.conf")));

		addChild(IpsecBootstrap.class);

		ItemBase model = OpsContext.get().getInstance(ItemBase.class);
		String uuid = platformLayerClient.getOrCreateUuid(model).toString();

		// TODO: Rationalize between our complicated version that can open cloud ports, and this streamlined version
		for (Transport transport : Transport.all()) {
			{
				IptablesFilterEntry allowIKE = addChild(IptablesFilterEntry.class);
				allowIKE.port = 500;
				allowIKE.protocol = Protocol.Udp;
				allowIKE.ruleKey = transport.getKey() + "-ike-" + uuid;
				allowIKE.transport = transport;
			}

			{// TODO: Do we want to open NAT-T (4500?)
				IptablesFilterEntry allowEsp = addChild(IptablesFilterEntry.class);
				allowEsp.protocol = Protocol.Esp;
				allowEsp.ruleKey = transport.getKey() + "-esp-" + uuid;
				allowEsp.transport = transport;
			}

			// AH iptables allow doesn't seem to work
			// AllowProtocol allowAh = addChild(AllowProtocol.class);
			// allowAh.protocol = Protocol.Ah;
			// allowAh.uuid = "ah-" + uuid;

			{
				IptablesFilterPolicy allowPolicy = addChild(IptablesFilterPolicy.class);
				allowPolicy.direction = Direction.In;
				allowPolicy.policy = "ipsec";
				allowPolicy.ruleKey = transport.getKey() + "-ipsec-" + uuid;
				allowPolicy.transport = transport;
			}

		}
		addChild(ManagedService.build("racoon"));
	}
}
