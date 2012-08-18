package org.platformlayer.ops.metrics;

import javax.inject.Inject;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tree.OpsTreeBase;

public class MetricsInstall extends OpsTreeBase {

	@Inject
	MetricsManager metricsManager;

	@Handler
	public void handler() {
	}

	@Override
	protected void addChildren() throws OpsException {
		addChild(metricsManager.getInstallClass());
	}

}
