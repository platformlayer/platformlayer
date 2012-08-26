package org.platformlayer.metrics.client.codahale;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.DiscoverSingletonMetrics;
import org.platformlayer.metrics.HasMetrics;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricTimer;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.metrics.client.MetricClient;
import org.platformlayer.metrics.client.PlatformlayerMetricsReporter;
import org.platformlayer.metrics.client.ReportCacheMetrics;

import com.google.common.cache.Cache;
import com.google.inject.Injector;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class CodahaleMetricsSystem implements MetricsSystem {
	private static final Logger log = Logger.getLogger(CodahaleMetricsSystem.class);

	@Inject
	Injector injector;

	@Inject
	DiscoverSingletonMetrics metricsDiscovery;

	@Inject
	MetricClient metricClient;

	final MetricsRegistry registry;

	public CodahaleMetricsSystem(MetricsRegistry registry) {
		this.registry = registry;
	}

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

	@Override
	public MetricTimer buildNewTimer(MetricKey metricKey) {
		MetricName metricName = new MetricName(metricKey.getGroup(), metricKey.getTypeName(), metricKey.getName());
		return new MetricTimerAdapter(registry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS));
	}

}
