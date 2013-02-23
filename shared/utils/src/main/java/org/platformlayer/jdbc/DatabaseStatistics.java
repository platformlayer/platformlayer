package org.platformlayer.jdbc;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.platformlayer.metrics.ApacheTomcatMetricsReporter;
import org.platformlayer.metrics.BoneCpMetricsReporter;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.jolbox.bonecp.BoneCPDataSource;

@Singleton
public class DatabaseStatistics {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DatabaseStatistics.class);

	final Map<String, DataSource> dataSources = Maps.newHashMap();

	@Inject
	MetricRegistry metrics;

	public void register(String key, BoneCPDataSource pooledDataSource) {
		synchronized (dataSources) {
			if (dataSources.containsKey(key)) {
				throw new IllegalStateException();
			}

			dataSources.put(key, pooledDataSource);

			MetricKey metricKey = MetricKey.build(DatabaseStatistics.class, key);
			metrics.add(new BoneCpMetricsReporter(metricKey, pooledDataSource));
		}
	}

	public void register(String key, org.apache.tomcat.jdbc.pool.DataSource pooledDataSource) {
		synchronized (dataSources) {
			if (dataSources.containsKey(key)) {
				throw new IllegalStateException();
			}

			dataSources.put(key, pooledDataSource);

			MetricKey metricKey = MetricKey.build(DatabaseStatistics.class, key);
			metrics.add(new ApacheTomcatMetricsReporter(metricKey, pooledDataSource));
		}
	}

}
