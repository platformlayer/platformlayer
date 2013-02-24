package org.platformlayer.jdbc;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.platformlayer.metrics.TomcatJdbcPoolMetricsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class TomcatJdbcPoolDataSourceBuilder extends DataSourceBuilderBase {
	private static final Logger log = LoggerFactory.getLogger(TomcatJdbcPoolDataSourceBuilder.class);

	@Override
	public DataSource buildDataSource(String key, JdbcConfiguration jdbcConfig) {
		PoolProperties p = new PoolProperties();

		if (jdbcConfig.driverClassName != null) {
			p.setDriverClassName(jdbcConfig.driverClassName);
		}

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);
		p.setUrl(jdbcConfig.jdbcUrl);
		p.setUsername(jdbcConfig.username);
		p.setPassword(jdbcConfig.password);

		String sqlDebug = null;
		if (jdbcConfig.extraProperties != null) {
			sqlDebug = jdbcConfig.extraProperties.get("sql.debug");
		}

		Properties jdbcProperties = buildDbProperties(jdbcConfig);

		if (!jdbcProperties.isEmpty()) {
			try {
				p.setDbProperties(jdbcProperties);
			} catch (Exception e) {
				throw new IllegalStateException("Unable to set JDBC properties", e);
			}
		}

		if (!Strings.isNullOrEmpty(sqlDebug)) {
			throw new UnsupportedOperationException();
			// pooledDataSource.setLogStatementsEnabled(Boolean.parseBoolean(sqlDebug));
		}

		p.setMinIdle(1);
		p.setInitialSize(1);

		List<String> interceptors = Lists.newArrayList();
		interceptors.add("org.apache.tomcat.jdbc.pool.interceptor.StatementCache");
		if (!interceptors.isEmpty()) {
			p.setJdbcInterceptors(Joiner.on(';').join(interceptors));
		}

		// p.setJmxEnabled(true);
		// p.setTestWhileIdle(false);
		// p.setTestOnBorrow(true);
		// p.setValidationQuery("SELECT 1");
		// p.setTestOnReturn(true);
		// p.setValidationInterval(30000);
		// p.setTimeBetweenEvictionRunsMillis(30000);
		// p.setMaxActive(100);
		// p.setInitialSize(10);
		// p.setMaxWait(10000);
		// p.setRemoveAbandonedTimeout(60);
		// p.setMinEvictableIdleTimeMillis(30000);
		// p.setMinIdle(10);
		// p.setLogAbandoned(true);
		// p.setRemoveAbandoned(true);
		// p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
		// + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

		org.apache.tomcat.jdbc.pool.DataSource pooledDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		pooledDataSource.setPoolProperties(p);

		databaseStatistics.register(key, pooledDataSource,
				new TomcatJdbcPoolMetricsReporter(databaseStatistics.getMetricKey(key), pooledDataSource));

		return pooledDataSource;
	}

}
