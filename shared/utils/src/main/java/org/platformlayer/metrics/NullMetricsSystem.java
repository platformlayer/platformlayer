package org.platformlayer.metrics;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.jolbox.bonecp.BoneCPDataSource;

public class NullMetricsSystem implements MetricsSystem {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(NullMetricsSystem.class);

	@Override
	public void add(Class<?> context, String prefix, Cache<?, ?> cache) {
	}

	@Override
	public void addInjected(Class<?> injected) {
	}

	@Override
	public void discoverMetrics(Object o) {
	}

	@Override
	public void init() {
	}

	@Override
	public MetricTimer getTimer(MetricKey metricKey) {
		return null;
	}

	@Override
	public void add(Class<?> context, String prefix, BoneCPDataSource pool) {
	}
}
