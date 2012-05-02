package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.machines.PlatformLayerCloudContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.ops.kvm.host.KvmHost;

public class DirectHostController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(DirectHostController.class);

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	PlatformLayerCloudContext platformLayerCloudContext;

	@Inject
	ServiceContext service;

	@Inject
	OpsContext ops;

	@Handler
	public void handler(DirectHost lxcHost) throws OpsException, IOException {
		// String instanceId = lxcHost.getTags().findUnique(Tag.INSTANCE_ID);
		// if (instanceId == null) {
		// MachineCreationRequest request = new MachineCreationRequest();
		// request.imageId = null; // lxcHost.imageId;
		// request.cloud = lxcHost.machineSource;
		//
		// SshKey sshKey = service.getSshKey();
		// request.sshPublicKey = sshKey.getKeyPair().getPublic();
		//
		// request.tags = new Tags();
		// request.tags.add(ops.getOpsSystem().createPlatformLayerLink(lxcHost));
		//
		// Machine instance = platformLayerCloudContext.createInstance(request);
		//
		// String serverId = instance.getServerId();
		//
		// platformLayer.addTag(lxcHost, Tag.INSTANCE_ID, serverId);
		// }

	}

	@Override
	protected void addChildren() throws OpsException {
		DirectHost model = OpsContext.get().getInstance(DirectHost.class);

		// if (Strings.isEmpty(model.dnsName)) {
		// throw new IllegalArgumentException("dnsName must be specified");
		// }

		// DirectCloud cloud = platformLayer.getItem(model.cloud, DirectCloud.class);

		// We'd like to auto-gen the disk image, but there's no way to auto-specify the OS at the moment
		// String dnsName = "direct-host-" + model.getId();
		// InstanceBuilder instance = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
		// instance.cloud = cloud.machineSource;
		// instance.addTagToManaged = true;
		// addChild(instance);

		DirectTarget host;
		{
			host = addChild(DirectTarget.class);
			host.address = NetworkPoint.forPublicHostname(model.host);
			host.sshKey = service.getSshKey();
		}

		// TODO: Do we want to differentiate between an LXC host and a KVM host?
		host.addChild(PackageDependency.build("lxc"));

		// Useful for moving images around
		host.addChild(PackageDependency.build("bzip2"));
		host.addChild(PackageDependency.build("socat"));

		host.addChild(KvmHost.class);

		host.addChild(MountCgroups.build());

		String bridge = "br100"; // lxcHost.bridge;
		IpRange ipRange = IpRange.parse(model.ipRange);

		host.addChild(PackageDependency.build("bridge-utils"));

		host.addChild(NetworkBridge.build(bridge, ipRange));
	}

}
