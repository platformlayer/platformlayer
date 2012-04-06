package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.instances.InstanceBuilder;
import org.platformlayer.ops.machines.PlatformLayerCloudContext;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.service.cloud.direct.model.DirectHost;
import org.platformlayer.service.cloud.direct.ops.kvm.host.KvmHost;

public class DirectHostController extends OpsTreeBase {
    static final Logger log = Logger.getLogger(DirectHostController.class);

    @Inject
    PlatformLayerClient platformLayer;

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

        // We'd like to auto-gen the disk image, but there's no way to auto-specify the OS at the moment
        String dnsName = "lxc-" + model.getId();
        InstanceBuilder instance = InstanceBuilder.build(dnsName, DiskImageRecipeBuilder.buildDiskImageRecipe(this));
        instance.cloud = model.machineSource;
        instance.addTagToManaged = true;
        addChild(instance);

        instance.addChild(PackageDependency.build("lxc"));

        instance.addChild(injected(KvmHost.class));

        instance.addChild(MountCgroups.build());

        String bridge = "br0"; // lxcHost.bridge;
        IpRange ipRange = IpRange.parse(model.ipRange);

        instance.addChild(PackageDependency.build("bridge-utils"));

        instance.addChild(NetworkBridge.build(bridge, ipRange));
    }

}
