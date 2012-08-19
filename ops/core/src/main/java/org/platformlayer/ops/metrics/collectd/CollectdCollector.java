package org.platformlayer.ops.metrics.collectd;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.service.ManagedService;

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

}
