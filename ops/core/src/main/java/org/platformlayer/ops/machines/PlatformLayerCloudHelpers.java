package org.platformlayer.ops.machines;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.platformlayer.Filter;
import org.platformlayer.StateFilter;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemState;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ProviderHelper;
import org.platformlayer.ops.helpers.ProviderHelper.ProviderOf;
import org.platformlayer.ops.helpers.SshKeys;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.images.ImageStoreProvider;
import org.platformlayer.xaas.services.ModelClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PlatformLayerCloudHelpers {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerCloudHelpers.class);

	@Inject
	MultiCloudScheduler scheduler;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	SshKeys sshKeys;

	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	ProviderHelper providers;

	public Machine toMachine(InstanceBase instance) {
		return new PlatformLayerCloudMachine(platformLayer, instance);
	}

	public void terminateMachine(Machine machine) throws OpsException {
		if (machine == null) {
			throw new IllegalArgumentException();
		}

		InstanceBase instance = toInstance(machine);
		if (instance == null) {
			throw new IllegalStateException();
		}

		platformLayer.deleteItem(instance.getKey());
	}

	private InstanceBase toInstance(Machine machine) {
		PlatformLayerCloudMachine platformLayerCloudMachine = (PlatformLayerCloudMachine) machine;
		return platformLayerCloudMachine.machine;
	}

	public PublicEndpointBase createPublicEndpoint(InstanceBase instance, PlatformLayerKey parent) throws OpsException {
		MachineProvider cloudController = getCloud(instance.cloud);

		PublicEndpointBase endpoint = cloudController.buildEndpointTemplate();

		if (parent != null) {
			endpoint.getTags().add(Tag.buildParentTag(parent));
		}

		return endpoint;
		// throw new UnsupportedOperationException();
	}

	public Machine putInstanceByTag(MachineCreationRequest request, PlatformLayerKey parent, Tag uniqueTag)
			throws OpsException {
		InstanceBase machine = buildInstanceTemplate(request, parent);

		machine = platformLayer.putItemByTag(machine, uniqueTag);

		return toMachine(machine);
	}

	InstanceBase buildInstanceTemplate(MachineCreationRequest request, PlatformLayerKey parent) throws OpsException {
		MachineProvider targetCloud = scheduler.pickCloud(request);

		InstanceBase machine = targetCloud.buildInstanceTemplate(request);

		machine.sshPublicKey = SshKeys.serialize(request.sshPublicKey);

		machine.recipeId = request.recipeId;

		if (request.publicPorts != null) {
			if (machine.publicPorts == null) {
				machine.publicPorts = Lists.newArrayList();
			}
			machine.publicPorts.addAll(request.publicPorts);
		}

		machine.getTags().addAll(request.tags);

		if (parent != null) {
			machine.getTags().add(Tag.buildParentTag(parent));
		}
		machine.cloud = targetCloud.getModel().getKey();
		machine.hostPolicy = request.hostPolicy;

		String id = request.hostname;
		if (Strings.isNullOrEmpty(id)) {
			id = UUID.randomUUID().toString();
		}

		machine.setKey(PlatformLayerKey.fromId(id));
		return machine;
	}

	public List<InstanceBase> findMachines(Tag tag) throws OpsException {
		List<InstanceBase> machines = Lists.newArrayList();

		boolean showDeleted = false;

		Filter filter = TagFilter.byTag(tag);
		if (!showDeleted) {
			filter = Filter.and(filter, StateFilter.exclude(ManagedItemState.DELETED));
		}

		// TODO: Fix this!!
		for (ModelClass<? extends InstanceBase> modelClass : serviceProviderHelpers
				.getModelSubclasses(InstanceBase.class)) {
			for (InstanceBase machine : platformLayer.listItems(modelClass.getJavaClass(), filter)) {
				machines.add(machine);
			}
		}

		// machines.addAll(platformLayer.listItems(DirectInstance.class, tag));
		// machines.addAll(platformLayer.listItems(RawInstance.class, tag));
		// machines.addAll(platformLayer.listItems(OpenstackInstance.class, tag));

		return machines;
	}

	public MachineProvider getCloud(PlatformLayerKey key) throws OpsException {
		MachineProvider cloud = findCloud(key);
		if (cloud == null) {
			throw new OpsException("Cannot find cloud: " + key);
		}
		return cloud;
	}

	private MachineProvider findCloud(PlatformLayerKey key) throws OpsException {
		ItemBase item = platformLayer.findItem(key);
		return providers.toInterface(item, MachineProvider.class);
	}

	public List<MachineProvider> findClouds() throws OpsException {
		List<MachineProvider> clouds = Lists.newArrayList();
		for (ProviderOf<MachineProvider> p : providers.listItemsProviding(MachineProvider.class)) {
			clouds.add(p.get());
		}
		return clouds;
	}

	public ImageStore getImageStore(MachineProvider targetCloud) throws OpsException {
		return targetCloud.getImageStore();
	}

	public ImageStore getImageStore(ItemBase item) throws OpsException {
		return getImageStore(providers.toInterface(item, MachineProvider.class));
	}

	public ImageStore getGenericImageStore() throws OpsException {
		for (ProviderOf<ImageStoreProvider> p : providers.listItemsProviding(ImageStoreProvider.class)) {
			ImageStore imageStore = p.get().getImageStore();

			if (imageStore != null) {
				return imageStore;
			}
		}
		return null;
	}

	public StorageConfiguration getStorageConfiguration(MachineProvider targetCloud) throws OpsException {
		return targetCloud.getStorageConfiguration();
	}

	public StorageConfiguration getStorageConfiguration(Machine machine) throws OpsException {
		InstanceBase instance = toInstance(machine);

		PlatformLayerKey cloudKey = instance.cloud;

		MachineProvider cloud = getCloud(cloudKey);

		return getStorageConfiguration(cloud);
	}

}
