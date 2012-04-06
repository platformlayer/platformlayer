package org.platformlayer.service.wordpress.ops;

import java.io.File;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;

public class ApacheBootstrap extends OpsTreeBase {
    @Handler
    public void handler() throws OpsException {
        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        target.rm(new File("/etc/apache2/sites-enabled/000-default"));
    }

    public static ApacheBootstrap build() {
        ApacheBootstrap apacheModule = Injection.getInstance(ApacheBootstrap.class);
        return apacheModule;
    }

    @Override
    protected void addChildren() throws OpsException {
    }
}
