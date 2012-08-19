package org.platformlayer.ops.metrics;

import org.platformlayer.ops.OpsException;

public interface MetricsManager {
	public Class<?> getInstallClass();

	public void addMetricsInstance(MetricsInstance service) throws OpsException;
}
