package org.openstack.service.nginx.ops;

import java.io.IOException;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NginxBackendController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(NginxBackendController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(injected(NginxBackendConfiguration.class));
		addChild(injected(NginxSites.class));
	}
}
