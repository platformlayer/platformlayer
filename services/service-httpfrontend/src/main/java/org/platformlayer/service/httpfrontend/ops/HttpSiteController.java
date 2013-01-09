package org.platformlayer.service.httpfrontend.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSiteController extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(HttpSiteController.class);

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(HttpSiteConfiguration.class);
		addChild(HttpDnsConfiguration.class);
	}
}
