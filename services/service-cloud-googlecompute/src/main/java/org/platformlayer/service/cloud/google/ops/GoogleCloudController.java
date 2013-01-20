package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;
import java.util.List;

import org.platformlayer.core.model.HostPolicyTag;
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
import org.platformlayer.service.cloud.google.model.GoogleCloud;
import org.platformlayer.service.cloud.google.model.GoogleCloudInstance;
import org.platformlayer.service.cloud.google.model.GoogleCloudPublicEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class GoogleCloudController extends OpsTreeBase implements MachineProvider {

	private static final Logger log = LoggerFactory.getLogger(GoogleCloudController.class);

	@Bound
	GoogleCloud model;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	public ImageStore getImageStore() throws OpsException {
		throw new UnsupportedOperationException();

		// GoogleCloud cloud = (GoogleCloud) cloudObject;
		//
		// return cloudContext.getImageStore(cloud);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public StorageConfiguration getStorageConfiguration() throws OpsException {
		throw new UnsupportedOperationException();

		// GoogleCloud cloud = (GoogleCloud) cloudObject;
		//
		// String authUrl = cloud.endpoint;
		//
		// OpenstackCredentials credentials = new OpenstackCredentials(authUrl, cloud.username,
		// cloud.password.plaintext(), cloud.tenant);
		// StorageConfiguration config = new StorageConfiguration(credentials);
		// return config;
	}

	@Override
	public InstanceBase buildInstanceTemplate(MachineCreationRequest request) {
		GoogleCloudInstance rawMachine = new GoogleCloudInstance();

		rawMachine.minimumMemoryMb = request.minimumMemoryMB;
		rawMachine.hostname = request.hostname;

		return rawMachine;
	}

	@Override
	public PublicEndpointBase buildEndpointTemplate() {
		return new GoogleCloudPublicEndpoint();
	}

	@Override
	public ItemBase getModel() {
		return model;
	}

	@Override
	public float getPrice(MachineCreationRequest request) {
		List<String> unsatisfied = HostPolicyTag.satisfy(request.hostPolicy, model.getTags());
		if (unsatisfied.isEmpty()) {
			log.info("Cannot satisfy requirements: " + Joiner.on(",").join(unsatisfied));
			return Float.POSITIVE_INFINITY;
		}

		return 50;
	}
}
