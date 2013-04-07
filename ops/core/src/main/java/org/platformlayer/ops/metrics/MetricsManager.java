package org.platformlayer.ops.metrics;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ops.OpsException;

public interface MetricsManager {
	public Class<?> getInstallClass();

	public void addMetricsInstance(MetricsInstance service) throws OpsException;

	public PlatformLayerKey findMetricsServer() throws OpsException;

	// public void addConfigurationProperties(PlatformLayerKey itemKey, Map<String, String> properties);
}
