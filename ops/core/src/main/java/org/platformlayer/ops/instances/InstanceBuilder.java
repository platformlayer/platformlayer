package org.platformlayer.ops.instances;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.TimeSpan;
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
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.bootstrap.InstanceBootstrap;
import org.platformlayer.ops.dns.DnsResolver;
import org.platformlayer.ops.helpers.ImageFactory;
import org.platformlayer.ops.helpers.InstanceHelpers;
import org.platformlayer.ops.helpers.InstanceSupervisor;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.helpers.SshKey;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
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
	OpsContext ops;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	CloudContext cloudContext;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Inject
	InstanceHelpers instances;

	@Handler
	public void doOperation() throws OpsException, IOException {
		ItemBase item = ops.getInstance(ItemBase.class);

		Tag parentTag = Tag.buildParentTag(item.getKey());

		PersistentInstance persistentInstanceTemplate = buildPersistentInstanceTemplate();

		persistentInstanceTemplate.getTags().add(parentTag);

		// Set during doOperation
		Machine machine = null;
		PersistentInstance persistentInstance = null;
		InstanceBase instance = null;
		OpsTarget target = null;

		persistentInstance = getOrCreate(parentTag, persistentInstanceTemplate);

		if (persistentInstance != null) {
			// We have to connect to the underlying machine not-via-DNS for Dns service => use instance id
			// TODO: Should we always use the instance id??

			instance = instances.findInstance(persistentInstance);
			if (instance == null && !OpsContext.isDelete()) {
				// A machine has not (yet) been assigned
				throw new OpsException("Machine is not yet built").setRetry(TimeSpan.ONE_MINUTE);
			}
		}

		if (instance != null) {
			machine = cloudHelpers.toMachine(instance);
		}

		if (addTagToManaged && !OpsContext.isDelete()) {
			// Add tag with instance id to persistent instance (very helpful for
			// DNS service!)
			PlatformLayerKey machineKey = machine.getKey();
			platformLayer.addTag(item.getKey(), Tag.INSTANCE_KEY.build(machineKey));
		}

		SshKey sshKey = service.getSshKey();
		if (machine != null) {
			if (OpsContext.isDelete() && machine.isTerminated()) {
				target = null;
			} else {
				target = machine.getTarget(sshKey);
			}
		}

		RecursionState recursion = getRecursionState();
		if (OpsContext.isDelete() && machine == null) {
			// Don't recurse into no machine :-)
			recursion.setPreventRecursion(true);
		}

		recursion.pushChildScope(Machine.class, machine);
		recursion.pushChildScope(PersistentInstance.class, persistentInstance);
		recursion.pushChildScope(InstanceBase.class, instance);
		recursion.pushChildScope(OpsTarget.class, target);
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
		persistentInstanceTemplate.setRecipe(recipe.getKey());

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

	PersistentInstance getOrCreate(Tag tag, PersistentInstance persistentInstance) throws PlatformLayerClientException,
			OpsException {
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
				platformLayer.ensureDeleted(foundPersistentInstance);
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
		addChild(InstanceBootstrap.class);

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
