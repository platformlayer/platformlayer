package org.platformlayer.service.cloud.direct.ops;

import javax.inject.Inject;

import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.PolicyChecker;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectCloud;
import org.platformlayer.service.cloud.direct.model.DirectInstance;
import org.platformlayer.service.cloud.direct.model.DirectPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectCloudController extends OpsTreeBase implements MachineProvider {
	static final Logger log = LoggerFactory.getLogger(DirectCloudController.class);

	@Bound
	DirectCloud model;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() {
	}

	@Override
	public ImageStore getImageStore() throws OpsException {
		return cloudHelpers.getGenericImageStore();
	}

	@Override
	public StorageConfiguration getStorageConfiguration() throws OpsException {
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

	@Override
	public ItemBase getModel() {
		return model;
	}

	@Override
	public float getPrice(MachineCreationRequest request) {
		if (!PolicyChecker.isSatisfied(request.hostPolicy, model.getTags())) {
			return Float.POSITIVE_INFINITY;
		}

		return 10;
	}

}
