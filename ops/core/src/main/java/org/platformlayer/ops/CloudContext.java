package org.platformlayer.ops;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ops.images.ImageStore;

import com.google.inject.ProvidedBy;

@ProvidedBy(CloudContextProvider.class)
public interface CloudContext {
	// Instance services
	Machine createInstance(MachineCreationRequest request, PlatformLayerKey parent) throws OpsException;

	// Machine toMachine(InstanceBase instance) throws OpsException;

	InstanceBase findInstanceByInstanceKey(PlatformLayerKey instanceKey) throws OpsException;

	Machine findMachine(Tag cloudInstanceTag) throws OpsException;

	Machine refreshMachine(Machine machine) throws OpsException;

	// Image services
	ImageStore getImageStore(MachineCloudBase targetCloud) throws OpsException;

	// Misc
	public void validate() throws OpsException;

	// KeyPair generateSshKeyPair(String sshKeyName) throws OpsException;

	// Security
	// TODO: Remove??
	void ensureCreatedSecurityGroup(String securityGroup) throws OpsException;

	void ensurePortOpen(String securityGroup, String protocol, int port) throws OpsException;
}
