package org.platformlayer.ops.machines;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;

public interface MachineProvider {
	public ImageStore getImageStore() throws OpsException;

	public StorageConfiguration getStorageConfiguration() throws OpsException;

	public InstanceBase buildInstanceTemplate(MachineCreationRequest request);

	public PublicEndpointBase buildEndpointTemplate();

	public ItemBase getModel();

	public float getPrice(MachineCreationRequest request);
}
