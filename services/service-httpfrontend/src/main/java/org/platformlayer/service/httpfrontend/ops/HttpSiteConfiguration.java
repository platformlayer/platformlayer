package org.platformlayer.service.httpfrontend.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.tree.ForEach;
import org.platformlayer.ops.tree.LateBound;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;

public class HttpSiteConfiguration extends OpsTreeBase implements CustomRecursor {
	static final Logger log = Logger.getLogger(HttpSiteConfiguration.class);

	@Handler
	public void handler() {
	}

	@Inject
	ServiceContext service;

	@Override
	protected void addChildren() throws OpsException {
		addChild(LateBound.of(HostConfigFile.class));
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		ForEach recursor = Injection.getInstance(ForEach.class);

		recursor.doRecursion(this, service.getSshKey(), HttpServer.class, HttpSite.class);
	}

}
