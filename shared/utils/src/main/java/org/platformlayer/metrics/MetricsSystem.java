package org.platformlayer.metrics;

import com.google.common.cache.Cache;
import com.jolbox.bonecp.BoneCPDataSource;

public interface MetricsSystem {
	void add(Class<?> context, String prefix, Cache<?, ?> cache);

	void add(Class<?> context, String prefix, BoneCPDataSource pool);

	void addInjected(Class<?> injected);

	void discoverMetrics(Object o);

	void init();

	MetricTimer getTimer(MetricKey metricKey);
}
