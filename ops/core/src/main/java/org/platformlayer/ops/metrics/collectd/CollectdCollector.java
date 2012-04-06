package org.platformlayer.ops.metrics.collectd;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;

public class CollectdCollector extends CollectdCommon {
    @Handler
    public void doOperation() throws OpsException {
    }

    @Override
    protected void addChildren() throws OpsException {
        addBasicStructure();

        addStandardMetrics();

        addMetricSet("network-send");

        addChild(ManagedService.build("collectd"));
    }

    public static CollectdCollector build() {
        return Injection.getInstance(CollectdCollector.class);
    }

}
