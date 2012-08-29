package org.platformlayer.metrics;

import org.apache.log4j.Logger;

public class NullMetricsSystem implements MetricsSystem {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(NullMetricsSystem.class);

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
	public MetricHistogram getHistogram(MetricKey metricKey) {
		return null;
	}

	@Override
	public MetricMeter getCounter(MetricKey build) {
		return null;
	}

	@Override
	public void add(MetricsReporter metricsReporter) {
	}
}
