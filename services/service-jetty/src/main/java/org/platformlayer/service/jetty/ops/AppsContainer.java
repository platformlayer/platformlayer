package org.platformlayer.service.jetty.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

/**
 * A 'well known' object, into which we can add Jetty apps
 * 
 */
public class AppsContainer extends OpsTreeBase {

	@Handler
	public void handler() {

	}

	@Override
	protected void addChildren() throws OpsException {

	}

}
