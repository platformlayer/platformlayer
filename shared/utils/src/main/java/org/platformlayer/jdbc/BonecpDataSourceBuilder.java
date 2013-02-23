package org.platformlayer.jdbc;

import java.util.Properties;

import javax.sql.DataSource;

import org.platformlayer.metrics.BoneCpMetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.jolbox.bonecp.BoneCPDataSource;

public class BonecpDataSourceBuilder extends DataSourceBuilderBase {
	private static final Logger log = LoggerFactory.getLogger(BonecpDataSourceBuilder.class);

	@Override
	public DataSource buildDataSource(String key, JdbcConfiguration jdbcConfig) {
		BoneCPDataSource pooledDataSource = new BoneCPDataSource();

		if (jdbcConfig.driverClassName != null) {
			try {
				Class.forName(jdbcConfig.driverClassName);
			} catch (ClassNotFoundException e) {
				log.warn("Ignoring error loading DB driver", e);
			}
		}

		// pooledDataSource.setDriverClassName(getProperty(keyPrefix + "driverClassName"));
		pooledDataSource.setJdbcUrl(jdbcConfig.jdbcUrl);
		pooledDataSource.setUsername(jdbcConfig.username);
		pooledDataSource.setPassword(jdbcConfig.password);

		String sqlDebug = null;

		if (jdbcConfig.extraProperties != null) {
			sqlDebug = jdbcConfig.extraProperties.get("sql.debug");
		}

		Properties jdbcProperties = buildDbProperties(jdbcConfig);

		if (!jdbcProperties.isEmpty()) {
			try {
				pooledDataSource.setDriverProperties(jdbcProperties);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to set JDBC properties", e);
			}
		}

		if (!Strings.isNullOrEmpty(sqlDebug)) {
			pooledDataSource.setLogStatementsEnabled(Boolean.parseBoolean(sqlDebug));
		}

		pooledDataSource.setPartitionCount(1);
		pooledDataSource.setMinConnectionsPerPartition(1);

		// Don't auto-acquire new connections
		// TODO: This is broken!!!
		pooledDataSource.setPoolAvailabilityThreshold(0);

		// Enable statement caching
		pooledDataSource.setStatementsCacheSize(32);

		// Track statistics
		pooledDataSource.setStatisticsEnabled(true);

		pooledDataSource.setConnectionHook(new BoneCPConnectionHook(key));

		databaseStatistics.register(key, pooledDataSource,
				new BoneCpMetricsReporter(databaseStatistics.getMetricKey(key), pooledDataSource));

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);

		return pooledDataSource;
	}

}
