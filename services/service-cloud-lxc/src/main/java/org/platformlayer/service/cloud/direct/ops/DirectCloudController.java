package org.platformlayer.service.cloud.direct.ops;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;

public class DirectCloudController extends OpsTreeBase implements CloudController {
	static final Logger log = Logger.getLogger(DirectCloudController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
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
}
