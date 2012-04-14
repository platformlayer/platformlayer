package org.openstack.service.nginx.ops;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class NginxBackendController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(NginxBackendController.class);

	@Handler
	public void handler() throws OpsException, IOException {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(injected(NginxBackendConfiguration.class));
		addChild(injected(NginxSites.class));
	}
}
