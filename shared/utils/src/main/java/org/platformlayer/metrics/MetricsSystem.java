package org.platformlayer.metrics;

public interface MetricsSystem {
	void addInjected(Class<?> injected);

	void discoverMetrics(Object o);

	void init();

	MetricTimer getTimer(MetricKey metricKey);

	MetricHistogram getHistogram(MetricKey metricKey);

	MetricMeter getCounter(MetricKey metricKey);

	void add(MetricsReporter metricsReporter);
}
