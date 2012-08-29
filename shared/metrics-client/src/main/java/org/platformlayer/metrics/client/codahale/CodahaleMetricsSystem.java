package org.platformlayer.metrics.client.codahale;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.DiscoverSingletonMetrics;
import org.platformlayer.metrics.HasMetrics;
import org.platformlayer.metrics.MetricHistogram;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricMeter;
import org.platformlayer.metrics.MetricTimer;
import org.platformlayer.metrics.MetricsReporter;
import org.platformlayer.metrics.MetricsSystem;
import org.platformlayer.metrics.client.MetricClient;
import org.platformlayer.metrics.client.PlatformlayerMetricsReporter;

import com.google.common.base.Joiner;
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

	final PlatformlayerMetricsReporter reporter;

	public CodahaleMetricsSystem(MetricsRegistry registry) {
		this.registry = registry;

		this.reporter = PlatformlayerMetricsReporter.enable(10, TimeUnit.SECONDS, metricClient);
	}

	@Override
	public void addInjected(Class<?> injectedType) {
		Object instance = injector.getInstance(injectedType);

		discoverMetrics(instance);
	}

	@Override
	public void discoverMetrics(Object o) {
		if (o instanceof MetricsReporter) {
			log.debug("Adding metrics from " + o);
			add((MetricsReporter) o);
		}

		if (o instanceof HasMetrics) {
			log.debug("Discovering metrics on" + o);
			((HasMetrics) o).discoverMetrics(this);
		} else {
			log.debug("No metrics discovered on " + o);
		}
	}

	@Override
	public void init() {
		metricsDiscovery.discover();
	}

	@Override
	public MetricTimer getTimer(MetricKey metricKey) {
		MetricName metricName = toMetricName(metricKey);
		return new MetricTimerAdapter(registry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS));
	}

	private MetricName toMetricName(MetricKey metricKey) {
		Class<?> sourceClass = metricKey.getSourceClass();

		String[] path = metricKey.getPath();
		String name = Joiner.on('.').join(path);

		return new MetricName(sourceClass, name);
	}

	@Override
	public MetricHistogram getHistogram(MetricKey metricKey) {
		MetricName metricName = toMetricName(metricKey);
		boolean biased = true;
		return new MetricHistogramAdapter(registry.newHistogram(metricName, biased));
	}

	@Override
	public MetricMeter getCounter(MetricKey metricKey) {
		MetricName metricName = toMetricName(metricKey);
		String eventType = "events";
		return new MetricMeterAdapter(registry.newMeter(metricName, eventType, TimeUnit.SECONDS));
	}

	@Override
	public void add(MetricsReporter metricsReporter) {
		reporter.addReporter(metricsReporter);
	}
}
