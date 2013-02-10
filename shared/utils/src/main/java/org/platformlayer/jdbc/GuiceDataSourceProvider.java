package org.platformlayer.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.common.base.Strings;
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
				dataSource = buildDataSource(jdbcConfig);
			}
			return dataSource;
		}
	}

	DataSource buildDataSource(JdbcConfiguration jdbcConfig) {
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

		if (!Strings.isNullOrEmpty(sqlDebug)) {
			pooledDataSource.setLogStatementsEnabled(Boolean.parseBoolean(sqlDebug));
		}

		// Enable statement caching
		pooledDataSource.setStatementsCacheSize(32);

		// Track statistics
		pooledDataSource.setStatisticsEnabled(true);

		pooledDataSource.setConnectionHook(new BoneCPConnectionHook(key));

		databaseStatistics.register(key, pooledDataSource);

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);

		return pooledDataSource;
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