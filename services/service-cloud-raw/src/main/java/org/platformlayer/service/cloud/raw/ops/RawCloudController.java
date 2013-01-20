package org.platformlayer.service.cloud.raw.ops;

import java.io.IOException;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.raw.model.RawCloud;
import org.platformlayer.service.cloud.raw.model.RawInstance;
import org.platformlayer.service.cloud.raw.model.RawPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawCloudController extends OpsTreeBase implements MachineProvider {
	private static final Logger log = LoggerFactory.getLogger(RawCloudController.class);

	@Bound
	RawCloud model;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public ImageStore getImageStore() throws OpsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public StorageConfiguration getStorageConfiguration() throws OpsException {
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

	@Override
	public ItemBase getModel() {
		return model;
	}

	@Override
	public float getPrice(MachineCreationRequest request) {
		return 100;
	}
}
