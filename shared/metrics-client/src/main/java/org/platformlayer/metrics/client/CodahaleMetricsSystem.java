package org.platformlayer.metrics.client;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.DiscoverSingletonMetrics;
import org.platformlayer.metrics.HasMetrics;
import org.platformlayer.metrics.MetricsSystem;

import com.google.common.cache.Cache;
import com.google.inject.Injector;

public class CodahaleMetricsSystem implements MetricsSystem {
	private static final Logger log = Logger.getLogger(CodahaleMetricsSystem.class);

	@Inject
	Injector injector;

	@Inject
	DiscoverSingletonMetrics metricsDiscovery;

	@Inject
	MetricClient metricClient;

	@Override
	public void add(Class<?> context, String prefix, Cache<?, ?> cache) {
		ReportCacheMetrics reporter = new ReportCacheMetrics(context, prefix, cache);
		reporter.init();
	}

	@Override
	public void addInjected(Class<?> injectedType) {
		Object instance = injector.getInstance(injectedType);

		discoverMetrics(instance);
	}

	@Override
	public void discoverMetrics(Object o) {
		if (o instanceof HasMetrics) {
			log.debug("Adding metrics from " + o);
			((HasMetrics) o).addMetrics(this);
		} else {
			log.debug("No metrics discovered on " + o);
		}
	}

	@Override
	public void init() {
		metricsDiscovery.discover();

		PlatformlayerMetricsReporter.enable(10, TimeUnit.SECONDS, metricClient);
	}

}
