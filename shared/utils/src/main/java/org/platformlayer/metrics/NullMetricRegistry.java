package org.platformlayer.metrics;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullMetricRegistry implements MetricRegistry {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NullMetricRegistry.class);

	@Override
	public void discoverMetrics(Object o) {
	}

	@Override
	public void add(MetricsSource metricsSource) {
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
	public List<MetricsSource> getAdditionalSources() {
		return Collections.emptyList();
	}
}
