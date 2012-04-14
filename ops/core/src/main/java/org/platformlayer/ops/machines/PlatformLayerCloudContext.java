package org.platformlayer.ops.machines;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.CloudContext;
import org.platformlayer.ops.Machine;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.xaas.services.ModelClass;

public class PlatformLayerCloudContext implements CloudContext {
	static final Logger log = Logger.getLogger(PlatformLayerCloudContext.class);

	@Inject
	PlatformLayerHelpers platformLayerClient;

	@Inject
	ServiceProviderHelpers serviceProviderHelpers;

	@Inject
	MultiCloudScheduler scheduler;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Override
	public Machine createInstance(MachineCreationRequest request, PlatformLayerKey parent) throws OpsException {
		Tag uniqueTag = Tag.buildParentTag(parent);

		return cloudHelpers.createInstance(request, parent, uniqueTag);
	}

	@Inject
	ServiceProviderHelpers serviceProviders;

	@Override
	public InstanceBase findInstanceByInstanceKey(PlatformLayerKey instanceKey) throws OpsException {
		ModelClass<?> modelClass = serviceProviders.getModelClass(instanceKey);

		InstanceBase machine = (InstanceBase) platformLayerClient.getItem(instanceKey, modelClass.getJavaClass());
		return machine;
	}

	@Override
	public Machine findMachine(Tag tag) throws OpsException {
		List<InstanceBase> machines = cloudHelpers.findMachines(tag);

		if (machines.size() == 0) {
			return null;
		}
		if (machines.size() == 1) {
			return cloudHelpers.toMachine(machines.get(0));
		}
		throw new OpsException("Found multiple machines with tag: " + tag);
	}

	@Override
	public void ensureCreatedSecurityGroup(String securityGroup) throws OpsException {
		log.warn("Security groups stubbed out");
	}

	@Override
	public void ensurePortOpen(String securityGroup, String protocol, int port) throws OpsException {
		log.warn("Security groups stubbed out");
	}

	// @Override
	// public boolean isImageFormatTar() {
	// return true;
	// }

	@Override
	public void validate() throws OpsException {
		// TODO: Implement
		log.warn("Validate stub-implemented for PlatformCloudContext");
	}

	@Override
	public ImageStore getImageStore(MachineCloudBase targetCloud) throws OpsException {
		ImageStore imageStore = cloudHelpers.getImageStore(targetCloud);
		// UserInfo userInfo = OpsContext.get().getUserInfo();
		// URL url = new URL(userInfo.getConfig().getRequiredString("image.url"));
		// String protocol = "ssh://";
		// if (!protocol.equals(url.getProtocol())) {
		// throw new OpsException("Expected SSH protocol for image store");
		// }

		// Machine machine = new OpaqueMachine(url.getHost());
		//
		// String user = url.getUserInfo();
		// if (user == null) {
		// user = "root"; // Or username associated with ssh key?
		// }
		//
		// KeyPair sshKey = userInfo.getConfig();
		// OpsTarget imageStoreHost = machine.getTarget(user, sshKey);
		// return new DirectImageStore(imageStoreHost);
		if (imageStore == null) {
			throw new OpsException("Image store not found (or none active)");
		}

		return imageStore;
	}

	public void terminate(ItemBase machine) throws OpsException {
		platformLayerClient.deleteItem(OpsSystem.toKey(machine));
	}

	@Override
	public Machine refreshMachine(Machine machine) throws OpsException {
		PlatformLayerKey key = machine.getKey();
		InstanceBase refreshed = findInstanceByInstanceKey(key);
		return cloudHelpers.toMachine(refreshed);

	}

}
