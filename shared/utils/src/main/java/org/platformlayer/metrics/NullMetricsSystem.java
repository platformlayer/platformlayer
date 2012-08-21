package org.platformlayer.metrics;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;

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
}
