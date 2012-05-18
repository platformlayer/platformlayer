package org.platformlayer.service.federation.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class FederatedServiceController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(FederatedServiceController.class);

	@Handler
	public void doOperation() {
	}

	@Override
	protected void addChildren() throws OpsException {

	}
}
