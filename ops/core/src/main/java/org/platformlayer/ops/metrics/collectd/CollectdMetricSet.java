package org.platformlayer.ops.metrics.collectd;

import java.io.File;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.model.metrics.MetricConfig;
import org.platformlayer.ops.tree.OpsTreeBase;

public class CollectdMetricSet extends OpsTreeBase {
    String metricKey;

    @Handler
    public void doOperation() throws OpsException {
    }

    MetricConfig metrics = null;

    public MetricConfig getMetricsInfo() throws OpsException {
        if (metrics == null) {
            metrics = ResourceUtils.findResource(getClass(), getMetricKey() + ".metrics", MetricConfig.class);
            if (metrics == null) {
                // Empty to stop reload attempts
                metrics = new MetricConfig();
            }
        }
        return metrics;
    }

    public static CollectdMetricSet build(String metricKey) {
        CollectdMetricSet metricSet = Injection.getInstance(CollectdMetricSet.class);
        metricSet.setMetricKey(metricKey);
        return metricSet;
    }

    public String getMetricKey() {
        return metricKey;
    }

    public void setMetricKey(String metricKey) {
        this.metricKey = metricKey;
    }

    @Override
    protected void addChildren() throws OpsException {
        File confDir = new File("/etc/collectd/conf");
        TemplatedFile confFile = TemplatedFile.build(Injection.getInstance(CollectdModelBuilder.class), new File(confDir, getMetricKey() + ".conf"));
        addChild(confFile);
    }
}
