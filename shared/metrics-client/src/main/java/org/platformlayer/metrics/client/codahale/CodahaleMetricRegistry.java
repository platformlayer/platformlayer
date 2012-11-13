package org.platformlayer.metrics.client.codahale;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.platformlayer.metrics.HasMetrics;
import org.platformlayer.metrics.MetricHistogram;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricMeter;
import org.platformlayer.metrics.MetricRegistry;
import org.platformlayer.metrics.MetricTimer;
import org.platformlayer.metrics.MetricsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

public class CodahaleMetricRegistry implements MetricRegistry {
	private static final Logger log = LoggerFactory.getLogger(CodahaleMetricRegistry.class);

	final MetricsRegistry registry;

	final CopyOnWriteArrayList<MetricsSource> reporters = Lists.newCopyOnWriteArrayList();

	CodahaleMetricRegistry(MetricsRegistry registry) {
		this.registry = registry;
	}

	private MetricName toMetricName(MetricKey metricKey) {
		Class<?> sourceClass = metricKey.getSourceClass();

		String[] path = metricKey.getPath();
		String name = Joiner.on('.').join(path);

		return new MetricName(sourceClass, name);
	}

	@Override
	public MetricTimer getTimer(MetricKey metricKey) {
		MetricName metricName = toMetricName(metricKey);
		return new MetricTimerAdapter(registry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS));
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
	public void discoverMetrics(Object o) {
		if (o instanceof MetricsSource) {
			log.debug("Adding metrics from " + o);
			add((MetricsSource) o);
		}

		if (o instanceof HasMetrics) {
			log.debug("Discovering metrics on" + o);
			((HasMetrics) o).discoverMetrics(this);
		}
	}

	@Override
	public void add(MetricsSource metricsSource) {
		reporters.add(metricsSource);
	}

	@Override
	public List<MetricsSource> getAdditionalSources() {
		return reporters;
	}

}
