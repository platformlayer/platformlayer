package org.platformlayer.ops.helpers;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.PlatformLayerHelpers;
import org.platformlayer.ops.machines.ServiceProviderHelpers;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

public class InstanceHelpers {
	static final Logger log = Logger.getLogger(InstanceHelpers.class);

	@Inject
	CloudContext cloud;

	@Inject
	PlatformLayerHelpers platformLayer;

	@Inject
	OpsSystem ops;

	@Inject
	ServiceContext service;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Inject
	ServiceProviderHelpers serviceProviders;

	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	@Inject
	SshKeys sshKeys;

	@Inject
	Provider<InstanceFinder> instanceFinderProvider;

	public InstanceBase findInstance(ItemBase item) throws OpsException {
		InstanceFinder instanceFinder = instanceFinderProvider.get();

		instanceFinder.visit(item);

		List<InstanceBase> instances = instanceFinder.getInstances();
		if (instances.size() == 0) {
			return null;
		}
		if (instances.size() == 1) {
			return instances.get(0);
		}

		throw new OpsException("Found multiple instances for " + item.getKey());

		//
		// // We have to connect to the underlying machine not-via-DNS for Dns service => use instance id
		// // TODO: Should we always use the instance id??
		// {
		// String instanceKey = tags.findUnique(Tag.INSTANCE_KEY);
		//
		// if (instanceKey != null) {
		// InstanceBase instance = cloud.findInstanceByInstanceKey(PlatformLayerKey.parse(instanceKey));
		// return instance;
		// }
		// }
		//
		// {
		// // TODO: Do we have to skip this if we've been passed a PersistentInstances?
		//
		// Tag parentTag = Tag.buildParentTag(modelKey);
		//
		//
		// // String conductorId = ops.buildUrl(modelKey);
		//
		// // // TODO: Fix this so that we don't get everything...
		// // for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class)) {
		// // String systemId = persistentInstance.getTags().findUnique(Tag.PARENT_ID);
		// // if (Objects.equal(conductorId, systemId)) {
		// // String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
		// // if (instanceKey != null) {
		// // return cloud.findMachineByInstanceKey(instanceKey);
		// // }
		// // }
		// // }
		//
		// for (PersistentInstance persistentInstance : platformLayer.listItems(PersistentInstance.class,
		// TagFilter.byTag(parentTag))) {
		// String instanceKey = persistentInstance.getTags().findUnique(Tag.INSTANCE_KEY);
		// if (instanceKey != null) {
		// return cloud.findInstanceByInstanceKey(PlatformLayerKey.parse(instanceKey));
		// }
		// }
		//
		// }
		//
		// return null;
	}

	public Machine findMachine(ItemBase item) throws OpsException {
		InstanceBase instance = findInstance(item);
		if (instance == null) {
			return null;
		}

		return cloudHelpers.toMachine(instance);
	}

	public Machine getMachine(ItemBase item) throws OpsException {
		return getMachine(item, true);
	}

	public Machine getMachine(ItemBase item, boolean required) throws OpsException {
		Machine machine = findMachine(item);
		if (required && machine == null) {
			throw new OpsException("Could not determine instance for: " + item);
		}
		return machine;
	}

	public List<Machine> getMachines(ItemBase item, boolean required) throws OpsException {
		Class<? extends ItemBase> javaClass = item.getClass();
		ModelClass<?> modelClass = serviceProviderDictionary.getModelClass(javaClass);
		Object controller = modelClass.getProvider().getController(javaClass);

		// TODO: Should we just recurse down through children?
		if (controller instanceof MachineCluster) {
			MachineCluster machineCluster = (MachineCluster) controller;
			return machineCluster.getMachines(item, required);
		} else {
			Machine machine = getMachine(item, required);
			if (machine == null) {
				return Collections.emptyList();
			}
			return Collections.singletonList(machine);
		}
	}

	public OpsTarget getTarget(ItemBase item) throws OpsException {
		Machine machine = getMachine(item);
		return getTarget(item, machine);
	}

	public OpsTarget getTarget(ItemBase item, Machine machine) throws OpsException {
		ServiceType serviceType = serviceProviders.getServiceType(item.getClass());

		// TODO: This is so evil...
		SshKey sshKey = sshKeys.findOtherServiceKey(serviceType);

		return machine.getTarget(sshKey);
	}

}
