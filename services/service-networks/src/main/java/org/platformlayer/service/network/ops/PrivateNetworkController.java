package org.platformlayer.service.network.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateNetworkController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(PrivateNetworkController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
	}
}
