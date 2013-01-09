package org.platformlayer.service.cloud.raw.ops;

import java.io.IOException;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawInstance;
import org.platformlayer.service.cloud.raw.model.RawPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawCloudController extends OpsTreeBase implements CloudController {

	private static final Logger log = LoggerFactory.getLogger(RawCloudController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public ImageStore getImageStore(MachineCloudBase cloud) throws OpsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloud) throws OpsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public InstanceBase buildInstanceTemplate(MachineCreationRequest request) {
		RawInstance rawMachine = new RawInstance();

		return rawMachine;
	}

	@Override
	public PublicEndpointBase buildEndpointTemplate() {
		return new RawPublicEndpoint();
	}
}
