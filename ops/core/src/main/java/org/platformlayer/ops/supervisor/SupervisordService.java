package org.platformlayer.ops.supervisor;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.PackageDependency;

public class SupervisordService extends OpsTreeBase {

    @Handler
    public void handler() {
    }

    @Override
    protected void addChildren() throws OpsException {
        addChild(PackageDependency.build("supervisor"));
    }

}
