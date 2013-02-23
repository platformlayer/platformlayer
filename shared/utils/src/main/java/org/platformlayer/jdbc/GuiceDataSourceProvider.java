package org.platformlayer.jdbc;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.jolbox.bonecp.BoneCPDataSource;

@Singleton
public class GuiceDataSourceProvider implements Provider<DataSource> {
	private static final Logger log = LoggerFactory.getLogger(GuiceDataSourceProvider.class);

	private final JdbcConfiguration jdbcConfig;

	private final String key;

	final DatabaseStatistics databaseStatistics;

	DataSource dataSource = null;

	@Override
	public DataSource get() {
		synchronized (this) {
			if (dataSource == null) {
				// dataSource = buildBoneCpDataSource(jdbcConfig);
				dataSource = buildTomcatJdbcPoolDataSource(jdbcConfig);
			}
			return dataSource;
		}
	}

	DataSource buildBoneCpDataSource(JdbcConfiguration jdbcConfig) {
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

		databaseStatistics.register(key, pooledDataSource);

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);

		return pooledDataSource;
	}

	DataSource buildTomcatJdbcPoolDataSource(JdbcConfiguration jdbcConfig) {
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
		p.setJdbcInterceptors(Joiner.on(';').join(interceptors));

		// p.setJmxEnabled(true);
		// p.setTestWhileIdle(false);
		// p.setTestOnBorrow(true);
		// p.setValidationQuery("SELECT 1");
		// p.setTestOnReturn(false);
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

		databaseStatistics.register(key, pooledDataSource);

		return pooledDataSource;
	}

	private Properties buildDbProperties(JdbcConfiguration jdbcConfig) {
		Properties jdbcProperties = new Properties();
		if (jdbcConfig.extraProperties != null) {
			jdbcProperties.putAll(jdbcConfig.extraProperties);
		}

		if (jdbcProperties.containsKey("ssl.keys")) {
			String trustKeys = (String) jdbcProperties.remove("ssl.keys");

			// http://jdbc.postgresql.org/documentation/80/ssl-factory.html
			jdbcProperties.put("sslfactory", TrustedKeysSSLSocketFactory.class.getName());
			jdbcProperties.put("sslfactoryarg", trustKeys);
		}
		return jdbcProperties;
	}

	@Singleton
	public static class Factory {
		@Inject
		Configuration configuration;
		@Inject
		DatabaseStatistics databaseStatistics;

		public GuiceDataSourceProvider create(String keyPrefix) {
			return new GuiceDataSourceProvider(configuration, databaseStatistics, keyPrefix);
		}
	}

	// @Inject
	public GuiceDataSourceProvider(Configuration configuration, DatabaseStatistics databaseStatistics,
	/* @Assisted */String key) {
		this.key = key;
		this.jdbcConfig = JdbcConfiguration.build(configuration, key);
		this.databaseStatistics = databaseStatistics;
	}

	public static Provider<DataSource> bind(final String key) {
		Provider<DataSource> provider = new Provider<DataSource>() {
			@Inject
			Factory factory;

			@Override
			public DataSource get() {
				if (factory == null) {
					throw new IllegalStateException("factory is null");
				}
				return factory.create(key).get();
			}
		};

		return provider;
	}

	public static Provider<? extends DataSource> bindJndi(final String key) {
		Provider<DataSource> provider = new Provider<DataSource>() {
			@Override
			public DataSource get() {
				try {
					InitialContext context = new InitialContext();
					DataSource dataSource = (DataSource) context.lookup(key);
					return dataSource;
				} catch (NamingException e) {
					throw new IllegalStateException("Error getting JDNI connection: " + key, e);
				}
			}
		};

		return provider;

	}

}