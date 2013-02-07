package org.platformlayer.metrics.client.codahale;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.platformlayer.metrics.DiscoverSingletonMetrics;
import org.platformlayer.metrics.MetricRegistry;
import org.platformlayer.metrics.MetricReporter;
import org.platformlayer.metrics.client.MetricClient;
import org.platformlayer.metrics.client.PlatformlayerMetricsReporter;

@Singleton
public class CodahaleMetricsReporter implements MetricReporter {

	@Inject
	DiscoverSingletonMetrics metricsDiscovery;

	@Inject
	MetricClient metricClient;

	@Inject
	MetricRegistry registry;

	@Override
	public void start() {
		metricsDiscovery.discover();

		PlatformlayerMetricsReporter.Builder builder = new PlatformlayerMetricsReporter.Builder();
		builder.period = 15;
		builder.periodUnit = TimeUnit.SECONDS;
		builder.metricSender = metricClient;
		builder.registry = registry;

		PlatformlayerMetricsReporter.start(builder);
	}

}
