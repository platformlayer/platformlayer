package org.platformlayer.metrics;

import com.google.common.cache.Cache;

public interface MetricsSystem {
	void add(Class<?> context, String prefix, Cache<?, ?> cache);

	void addInjected(Class<?> injected);

	void discoverMetrics(Object o);

	void init();

	MetricTimer getTimer(MetricKey metricKey);
}
