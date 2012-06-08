package org.platformlayer.ops.vpn;

import java.io.File;

import javax.inject.Inject;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SimpleFile;
import org.platformlayer.ops.firewall.Protocol;
import org.platformlayer.ops.firewall.simple.AllowProtocol;
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

		ItemBase model = OpsContext.get().getInstance(ItemBase.class);
		String uuid = platformLayerClient.getOrCreateUuid(model).toString();

		AllowProtocol allowEsp = addChild(AllowProtocol.class);
		allowEsp.protocol = Protocol.Esp;
		allowEsp.uuid = "esp-" + uuid;

		AllowProtocol allowAh = addChild(AllowProtocol.class);
		allowAh.protocol = Protocol.Ah;
		allowAh.uuid = "ah-" + uuid;

		addChild(ManagedService.build("racoon"));
	}
}
