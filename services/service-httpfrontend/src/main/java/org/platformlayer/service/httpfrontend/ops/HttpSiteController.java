package org.platformlayer.service.httpfrontend.ops;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class HttpSiteController extends OpsTreeBase {
	static final Logger log = Logger.getLogger(HttpSiteController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(HttpSiteConfiguration.class);
		addChild(HttpDnsConfiguration.class);
	}
}
