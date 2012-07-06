package org.platformlayer.service.cloud.direct.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectPublicEndpoint;

public class DirectCloudController extends OpsTreeBase implements CloudController {
	static final Logger log = Logger.getLogger(DirectCloudController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() {
	}

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Override
	public ImageStore getImageStore(MachineCloudBase cloudBase) throws OpsException {
		return cloudHelpers.getGenericImageStore();
	}

	@Override
	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloud) throws OpsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InstanceBase buildInstanceTemplate(MachineCreationRequest request) {
		DirectInstance directMachine = new DirectInstance();

		directMachine.minimumMemoryMb = request.minimumMemoryMB;
		directMachine.hostname = request.hostname;

		return directMachine;
	}

	@Override
	public PublicEndpointBase buildEndpointTemplate() {
		return new DirectPublicEndpoint();
	}
}
