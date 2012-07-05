package org.platformlayer.service.cloud.google.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;

public class GoogleCloudController extends OpsTreeBase implements CloudController {
	static final Logger log = Logger.getLogger(GoogleCloudController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	public ImageStore getImageStore(MachineCloudBase cloudObject) throws OpsException {
		throw new UnsupportedOperationException();

		// GoogleCloud cloud = (GoogleCloud) cloudObject;
		//
		// return cloudContext.getImageStore(cloud);
	}

	@Override
	protected void addChildren() throws OpsException {
	}

	@Override
	public StorageConfiguration getStorageConfiguration(MachineCloudBase cloudObject) throws OpsException {
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
}
