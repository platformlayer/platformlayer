package org.openstack.service.nginx.ops;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openstack.service.nginx.model.NginxFrontend;
import org.openstack.service.nginx.model.NginxService;
import org.platformlayer.ops.CustomRecursor;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.helpers.ServiceContext;
import org.platformlayer.ops.tree.OpsTreeBase;

public class NginxSites extends OpsTreeBase implements CustomRecursor {
    static final Logger log = Logger.getLogger(NginxSites.class);

    @Handler
    public void handler() {
    }

    @Inject
    ServiceContext service;

    @Override
    protected void addChildren() throws OpsException {
        addChild(injected(NginxSiteConfig.class));
        addChild(injected(NginxSiteEnabledSymlink.class));
        addChild(injected(NginxFrontendDns.class));
    }

    @Override
    public void doRecurseOperation() throws OpsException {
        ForEach recursor = Injection.getInstance(ForEach.class);

        recursor.doRecursion(this, service.getSshKey(), NginxService.class, NginxFrontend.class);
    }

}
