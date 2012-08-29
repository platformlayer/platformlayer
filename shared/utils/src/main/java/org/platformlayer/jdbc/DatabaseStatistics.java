package org.platformlayer.jdbc;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.platformlayer.metrics.BoneCpMetricsReporter;
import org.platformlayer.metrics.MetricKey;
import org.platformlayer.metrics.MetricsSystem;

import com.google.common.collect.Maps;
import com.jolbox.bonecp.BoneCPDataSource;

@Singleton
public class DatabaseStatistics {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DatabaseStatistics.class);

	final Map<String, BoneCPDataSource> dataSources = Maps.newHashMap();

	@Inject
	MetricsSystem metrics;

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

}
