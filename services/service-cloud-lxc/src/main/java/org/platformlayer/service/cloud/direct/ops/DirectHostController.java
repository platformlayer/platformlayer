package org.platformlayer.service.cloud.direct.ops;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.platformlayer.cas.CasStore;
import org.platformlayer.cas.CasStoreInfo;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpaqueMachine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.bootstrap.InstanceBootstrap;
import org.platformlayer.ops.cas.CasStoreProvider;
import org.platformlayer.ops.cas.OpsCasTarget;
import org.platformlayer.ops.cas.filesystem.FilesystemCasStore;
import org.platformlayer.ops.dns.DnsResolver;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.direct.PeerToPeerCopy;
import org.platformlayer.ops.machines.PlatformLayerCloudContext;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.networks.NetworkPoint;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.supervisor.ServiceManager;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.ops.kvm.host.KvmHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectHostController extends OpsTreeBase implements CasStoreProvider {
	static final Logger log = LoggerFactory.getLogger(DirectHostController.class);

	public static final File LXC_INSTANCE_DIR = new File("/var/instances/lxc");
	public static final File KVM_INSTANCE_DIR = new File("/var/instances/kvm");

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	PlatformLayerCloudContext platformLayerCloudContext;

	@Inject
	ServiceContext service;

	@Inject
	OpsContext ops;

	@Bound
	DirectHost model;

	@Inject
	SshKeys sshKeys;

	@Inject
	ServiceManager serviceManager;

	@Handler
	public void handler() throws OpsException, IOException {
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

		// TODO: It isn't quite right to call this InstanceBootstrap any more!
		host.addChild(InstanceBootstrap.class);

		host.addChild(DnsResolver.class);

		// Time synchronization is pretty important
		host.addChild(PackageDependency.build("ntp"));

		// TODO: Do we want to differentiate between an LXC host and a KVM host?
		host.addChild(PackageDependency.build("lxc"));

		host.addChild(ManagedDirectory.build(LXC_INSTANCE_DIR, "0755"));
		host.addChild(ManagedDirectory.build(KVM_INSTANCE_DIR, "0755"));

		// Useful for moving images around
		host.addChild(PackageDependency.build("bzip2"));

		PeerToPeerCopy peerToPeerCopy = Injection.getInstance(PeerToPeerCopy.class);
		peerToPeerCopy.addChildren(this);

		{
			PlatformLayerKey owner = model.getKey();
			serviceManager.addServiceInstall(owner, host);
		}

		host.addChild(KvmHost.class);

		host.addChild(MountCgroups.build());

		host.addChild(PackageDependency.build("bridge-utils"));

		host.addChild(NetworkBridge.class);
	}

	@Override
	public CasStore getCasStore() throws OpsException {
		// TODO: Getting the IP like this is evil
		NetworkPoint targetAddress;
		// if (host.getIpv6() != null) {
		// IpRange ipv6Range = IpV6Range.parse(host.getIpv6());
		// targetAddress = NetworkPoint.forPublicHostname(ipv6Range.getGatewayAddress());
		// } else {
		targetAddress = NetworkPoint.forPublicHostname(model.host);
		// }

		Machine machine = new OpaqueMachine(targetAddress);
		OpsTarget machineTarget = machine.getTarget(sshKeys.findOtherServiceKey(new ServiceType("machines-direct")));

		CasStoreInfo casStoreOptions = new CasStoreInfo(true);
		FilesystemCasStore store = new FilesystemCasStore(casStoreOptions, new OpsCasTarget(machineTarget));
		return store;
	}

}
