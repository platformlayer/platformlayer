package org.platformlayer.service.jetty.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;

public class AppsContainer extends OpsTreeBase {

    @Handler
    public void handler() {

    }

    @Override
    protected void addChildren() throws OpsException {

    }

    public static AppsContainer build() {
        return Injection.getInstance(AppsContainer.class);
    }

}
