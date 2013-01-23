package org.platformlayer.service.cloud.direct.ops;

import javax.inject.Inject;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.machines.PlatformLayerCloudHelpers;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.cloud.direct.model.DirectNetworkAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectNetworkAssignmentController extends OpsTreeBase {
	static final Logger log = LoggerFactory.getLogger(DirectNetworkAssignmentController.class);

	@Bound
	DirectNetworkAssignment model;

	@Inject
	PlatformLayerCloudHelpers cloudHelpers;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() {
	}

}
