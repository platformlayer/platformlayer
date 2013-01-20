package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.openstack.client.OpenstackCredentials;
import org.platformlayer.cas.CasStoreInfo;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.cas.CasStoreProvider;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.MachineProvider;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.model.OpenstackPublicEndpoint;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackCloudController extends OpsTreeBase implements MachineProvider, CasStoreProvider {

	private static final Logger log = LoggerFactory.getLogger(OpenstackCloudController.class);

	@Bound
	OpenstackCloud model;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	OpenstackCloudContext cloudContext;

	@Override
	public ImageStore getImageStore() throws OpsException {
		return cloudContext.getImageStore(model);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public StorageConfiguration getStorageConfiguration() throws OpsException {
		String authUrl = model.endpoint;

		OpenstackCredentials credentials = new OpenstackCredentials(authUrl, model.username,
				model.password.plaintext(), model.tenant);
		StorageConfiguration config = new OpenstackStorageConfiguration(credentials);
		return config;
	}

	@Override
	public InstanceBase buildInstanceTemplate(MachineCreationRequest request) {
		OpenstackInstance rawMachine = new OpenstackInstance();

		rawMachine.minimumMemoryMb = request.minimumMemoryMB;
		rawMachine.hostname = request.hostname;

		return rawMachine;
	}

	@Override
	public PublicEndpointBase buildEndpointTemplate() {
		return new OpenstackPublicEndpoint();
	}

	@Override
	public OpenstackCasStore getCasStore() {
		OpenstackCredentials credential = new OpenstackCredentials(model.endpoint, model.username,
				model.password.plaintext(), model.tenant);

		String containerName = "platformlayer-artifacts";
		return new OpenstackCasStore(new CasStoreInfo(false), credential, containerName);

	}

	@Override
	public ItemBase getModel() {
		return model;
	}

	@Override
	public float getPrice(MachineCreationRequest request) {
		return 50;
	}
}
