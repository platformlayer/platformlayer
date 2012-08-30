package org.platformlayer.metrics;

import java.util.List;

public interface MetricRegistry {
	void discoverMetrics(Object o);

	public void add(MetricsSource metricsSource);

	List<MetricsSource> getAdditionalSources();

	MetricTimer getTimer(MetricKey metricKey);

	MetricHistogram getHistogram(MetricKey metricKey);

	MetricMeter getCounter(MetricKey metricKey);
}
