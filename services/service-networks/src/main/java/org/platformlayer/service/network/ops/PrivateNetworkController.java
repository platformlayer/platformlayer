package org.platformlayer.service.network.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class PrivateNetworkController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(PrivateNetworkController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
