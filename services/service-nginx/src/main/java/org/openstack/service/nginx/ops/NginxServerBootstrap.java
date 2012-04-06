package org.openstack.service.nginx.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class NginxServerBootstrap {

    @Handler
    public void handler() throws OpsException {
        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        target.rm(new File("/etc/nginx/sites-enabled/default"));
    }

    public static NginxServerBootstrap build() {
        return Injection.getInstance(NginxServerBootstrap.class);
    }

}
