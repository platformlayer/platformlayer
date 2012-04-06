package org.platformlayer.ops.instances;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.core.model.HostPolicy;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.dns.DnsResolver;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.InstanceSupervisor;
import org.platformlayer.ops.helpers.PersistentInstances;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.instancesupervisor.v1.PersistentInstance;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class InstanceBuilder extends OpsTreeBase implements CustomRecursor {
    public String dnsName;
    public Tags tags;
    public Provider<DiskImageRecipe> diskImageRecipe;

    public int minimumMemoryMb = 256;

    public PlatformLayerKey cloud;

    public HostPolicy hostPolicy = new HostPolicy();

    @Deprecated
    // PublicPorts = make a reservation on a "public" IP
    // This can be a tag / hostPolicy
    public List<Integer> publicPorts = Lists.newArrayList();

    public boolean addTagToManaged = false;

    @Inject
    InstanceSupervisor instanceSupervisor;

    @Inject
    ImageFactory imageFactory;

    @Inject
    ServiceContext service;

    @Inject
    PersistentInstances persistentInstances;

    @Inject
    OpsContext ops;

    @Inject
    PlatformLayerHelpers platformLayer;

    @Inject
    CloudContext cloudContext;

    @Inject
    PlatformLayerCloudHelpers cloudHelpers;

    @Handler
    public void doOperation() throws OpsException, IOException {
        ItemBase item = ops.getInstance(ItemBase.class);

        Tag parentTag = ops.getOpsSystem().createParentTag(item);

        PersistentInstance persistentInstanceTemplate = buildPersistentInstanceTemplate();

        persistentInstanceTemplate.getTags().add(parentTag);

        // Set during doOperation
        Machine machine = null;
        PersistentInstance persistentInstance = null;
        InstanceBase instance = null;
        OpsTarget target = null;

        persistentInstance = getOrCreate(parentTag, persistentInstanceTemplate);

        if (persistentInstance != null) {
            instance = persistentInstances.getInstance(persistentInstance);
        }

        if (instance != null) {
            machine = cloudHelpers.toMachine(instance);
        }

        if (addTagToManaged && !OpsContext.isDelete()) {
            // Add tag with instance id to persistent instance (very helpful for
            // DNS service!)
            PlatformLayerKey machineKey = machine.getKey();
            platformLayer.addTag(OpsSystem.toKey(item), new Tag(Tag.INSTANCE_KEY, machineKey.getUrl()));
        }

        SshKey sshKey = service.getSshKey();
        if (machine != null) {
            target = machine.getTarget(sshKey);
        }

        pushChildScope(Machine.class, machine);
        pushChildScope(PersistentInstance.class, persistentInstance);
        pushChildScope(InstanceBase.class, instance);
        pushChildScope(OpsTarget.class, target);
    }

    private PersistentInstance buildPersistentInstanceTemplate() throws OpsException {
        SshKey sshKey = service.getSshKey();
        String securityGroup = service.getSecurityGroupName();
        DiskImageRecipe recipeTemplate = diskImageRecipe.get();
        if (recipeTemplate.getKey() == null) {
            // TODO: Something nicer than a UUID
            String recipeId = UUID.randomUUID().toString();
            recipeTemplate.setKey(PlatformLayerKey.fromId(recipeId));
        }

        DiskImageRecipe recipe = imageFactory.getOrCreateRecipe(recipeTemplate);

        PersistentInstance persistentInstanceTemplate = new PersistentInstance();
        persistentInstanceTemplate.setDnsName(dnsName);
        persistentInstanceTemplate.setSshPublicKey(SshKeys.serialize(sshKey.getKeyPair().getPublic()));
        persistentInstanceTemplate.setSecurityGroup(securityGroup);
        persistentInstanceTemplate.setMinimumRam(minimumMemoryMb);
        persistentInstanceTemplate.setCloud(cloud);
        persistentInstanceTemplate.setHostPolicy(hostPolicy);
        persistentInstanceTemplate.setRecipe(OpsSystem.toKey(recipe));

        String id = dnsName;
        if (Strings.isNullOrEmpty(id)) {
            id = UUID.randomUUID().toString();
        }
        persistentInstanceTemplate.setKey(PlatformLayerKey.fromId(id));

        for (int publicPort : publicPorts) {
            persistentInstanceTemplate.getPublicPorts().add(publicPort);
        }
        return persistentInstanceTemplate;
    }

    PersistentInstance getOrCreate(Tag tag, PersistentInstance persistentInstance) throws PlatformLayerClientException, OpsException {
        PersistentInstance foundPersistentInstance = instanceSupervisor.findPersistentInstance(tag);

        if (!OpsContext.isDelete()) {
            // We always PUT it (should be idempotent)
            // if (foundPersistentInstance == null) {
            PersistentInstance created = platformLayer.putItemByTag(persistentInstance, tag);
            foundPersistentInstance = created;
            // }

            // if (foundPersistentInstance == null) {
            // // String imageId = imageFactory.getOrCreateImage(recipe);
            // // persistentInstance.setImageId(imageId);
            //
            // Tags tags = persistentInstance.getTags();
            // tags.add(tag);
            //
            // try {
            // // TODO: Parent tag isn't getting set??
            // PersistentInstance created = platformLayer.createItem(persistentInstance);
            // foundPersistentInstance = created;
            // } catch (PlatformLayerClientException e) {
            // throw new OpsException("Error registering persistent instance", e);
            // }
            // }
            //
            // if (foundPersistentInstance == null) {
            // throw new IllegalStateException();
            // }
        }

        if (OpsContext.isDelete()) {
            if (foundPersistentInstance != null) {
                try {
                    platformLayer.deleteItem(OpsSystem.toKey(foundPersistentInstance));
                } catch (PlatformLayerClientException e) {
                    throw new OpsException("Error deleting persistent instance", e);
                }
            }
        }

        return foundPersistentInstance;
    }

    public static InstanceBuilder build(String dnsName, Provider<DiskImageRecipe> diskImageRecipe) {
        InstanceBuilder instance = Injection.getInstance(InstanceBuilder.class);
        instance.dnsName = dnsName;
        instance.diskImageRecipe = diskImageRecipe;
        return instance;
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(injected(InstanceBootstrap.class));

        addChild(injected(DnsResolver.class));

        String hostname = dnsName;
        if (Strings.isNullOrEmpty(hostname)) {
            // We always want to set a valid hostname
            ItemBase item = ops.getInstance(ItemBase.class);
            hostname = item.getId();
        }

        if (hostname != null) {
            addChild(ConfigureHostname.build(hostname));
        }
    }

}
