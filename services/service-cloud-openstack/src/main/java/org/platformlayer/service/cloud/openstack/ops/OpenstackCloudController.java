package org.platformlayer.service.cloud.openstack.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.openstack.client.OpenstackCredentials;
import org.platformlayer.cas.CasStoreInfo;
import org.platformlayer.core.model.InstanceBase;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.core.model.PublicEndpointBase;
import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.MachineCreationRequest;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.cas.CasStoreProvider;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.openstack.model.OpenstackCloud;
import org.platformlayer.service.cloud.openstack.model.OpenstackInstance;
import org.platformlayer.service.cloud.openstack.model.OpenstackPublicEndpoint;
import org.platformlayer.service.cloud.openstack.ops.openstack.OpenstackCloudContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenstackCloudController extends OpsTreeBase implements CloudController, CasStoreProvider {

	private static final Logger log = LoggerFactory.getLogger(OpenstackCloudController.class);

	@Bound
	OpenstackCloud model;

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	OpenstackCloudContext cloudContext;

	@Override
	public ImageStore getImageStore(MachineCloudBase cloudObject) throws OpsException {
		return cloudContext.getImageStore(model);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloudObject) throws OpsException {
		OpenstackCloud cloud = (OpenstackCloud) cloudObject;

		String authUrl = cloud.endpoint;

		OpenstackCredentials credentials = new OpenstackCredentials(authUrl, cloud.username,
				cloud.password.plaintext(), cloud.tenant);
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
}
