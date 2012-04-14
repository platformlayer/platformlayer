package org.platformlayer.ops.metrics.collectd;

import java.io.File;

import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.ManagedDirectory;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;

public abstract class CollectdCommon extends OpsTreeBase {
	public static final int COLLECTD_PORT = 25826;

	protected void addBasicStructure() throws OpsException {
		addChild(PackageDependency.build("collectd"));

		File confBase = new File("/etc/collectd");

		addChild(ManagedDirectory.build(new File(confBase, "conf"), "755"));
		addChild(ManagedDirectory.build(new File(confBase, "filters"), "755"));
		addChild(ManagedDirectory.build(new File(confBase, "thresholds"), "755"));

		addChild(TemplatedFile.build(Injection.getInstance(CollectdModelBuilder.class), new File(confBase,
				"collectd.conf")));
	}

	protected void addStandardMetrics() throws OpsException {
		addMetricSet("cpu");
		addMetricSet("df");
		addMetricSet("swap");
		addMetricSet("syslog");
	}

	protected void addMetricSet(String key) throws OpsException {
		CollectdMetricSet metricSet = CollectdMetricSet.build(key);
		addChild(metricSet);
	}

}
