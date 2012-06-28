package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.client.OpenstackCredentials;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.google.model.GoogleCloud;
import org.platformlayer.service.cloud.google.ops.openstack.OpenstackCloudContext;

public class GoogleCloudController extends OpsTreeBase implements CloudController {
	static final Logger log = Logger.getLogger(GoogleCloudController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Inject
	OpenstackCloudContext cloudContext;

	@Override
	public ImageStore getImageStore(MachineCloudBase cloudObject) throws OpsException {
		GoogleCloud cloud = (GoogleCloud) cloudObject;

		return cloudContext.getImageStore(cloud);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloudObject) throws OpsException {
		GoogleCloud cloud = (GoogleCloud) cloudObject;

		String authUrl = cloud.endpoint;

		OpenstackCredentials credentials = new OpenstackCredentials(authUrl, cloud.username,
				cloud.password.plaintext(), cloud.tenant);
		StorageConfiguration config = new StorageConfiguration(credentials);
		return config;
	}
}
