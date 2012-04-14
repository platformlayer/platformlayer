package org.platformlayer.service.cloud.raw.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.MachineCloudBase;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.images.ImageStore;
import org.platformlayer.ops.machines.CloudController;
import org.platformlayer.ops.machines.StorageConfiguration;
import org.platformlayer.ops.tree.OpsTreeBase;

public class RawCloudController extends OpsTreeBase implements CloudController {
	static final Logger log = Logger.getLogger(RawCloudController.class);

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

}
