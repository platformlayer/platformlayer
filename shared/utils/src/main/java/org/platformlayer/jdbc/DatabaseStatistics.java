package org.platformlayer.jdbc;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricRegistry;
import org.platformlayer.metrics.MetricsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

@Singleton
public class DatabaseStatistics {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DatabaseStatistics.class);

	final Map<String, DataSource> dataSources = Maps.newHashMap();

	@Inject
	MetricRegistry metrics;

	public MetricKey getMetricKey(String key) {
		MetricKey metricKey = MetricKey.build(DatabaseStatistics.class, key);
		return metricKey;
	}

	public void register(String key, DataSource dataSource, MetricsSource metricsSource) {
		synchronized (dataSources) {
			if (dataSources.containsKey(key)) {
				throw new IllegalStateException();
			}

			dataSources.put(key, dataSource);

			metrics.add(metricsSource);
		}
	}
}
