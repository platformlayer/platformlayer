package org.platformlayer.service.httpfrontend.ops;

import javax.inject.Inject;

import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.tree.ForEach;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.httpfrontend.model.HttpServer;
import org.platformlayer.service.httpfrontend.model.HttpSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDnsConfiguration extends OpsTreeBase implements CustomRecursor {

	private static final Logger log = LoggerFactory.getLogger(HttpDnsConfiguration.class);

	@Handler
	public void handler() {
	}

	@Inject
	ServiceContext service;

	@Override
	protected void addChildren() throws OpsException {
		addChild(injected(HttpSiteDns.class));
	}

	@Override
	public void doRecurseOperation() throws OpsException {
		ForEach recursor = Injection.getInstance(ForEach.class);

		recursor.doRecursion(this, service.getSshKey(), HttpServer.class, HttpSite.class);
	}

}
