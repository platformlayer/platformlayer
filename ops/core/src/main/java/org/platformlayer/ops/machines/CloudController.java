package org.platformlayer.ops.machines;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;

public interface CloudController {
	public ImageStore getImageStore(MachineCloudBase cloud) throws OpsException;

	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloud) throws OpsException;

	public InstanceBase buildInstanceTemplate(MachineCreationRequest request);

	public PublicEndpointBase buildEndpointTemplate();
}
