package org.platformlayer.ops.metrics.collectd;

import org.apache.log4j.Logger;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;

public class CollectdSink extends CollectdCommon {
    static final Logger log = Logger.getLogger(CollectdSink.class);

    @Handler
    public void doOperation() throws OpsException {
    }

    @Override
    protected void addChildren() throws OpsException {
        addBasicStructure();

        addStandardMetrics();

        addMetricSet("rrdcached");
        addMetricSet("network-listen");

        addChild(ManagedService.build("collectd"));
    }

    public static CollectdSink build() {
        return Injection.getInstance(CollectdSink.class);
    }

}
