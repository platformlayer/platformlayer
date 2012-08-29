package org.platformlayer.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.platformlayer.config.Configuration;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.jolbox.bonecp.BoneCPDataSource;

@Singleton
public class GuiceDataSourceProvider implements Provider<DataSource> {
	private static final Logger log = Logger.getLogger(GuiceDataSourceProvider.class);

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

		if (jdbcConfig.extraProperties != null) {
			String logSql = jdbcConfig.extraProperties.getProperty("sql.debug", "false");
			pooledDataSource.setLogStatementsEnabled(Boolean.parseBoolean(logSql));
		}

		// Enable statement caching
		pooledDataSource.setStatementsCacheSize(32);

		// Track statistics
		pooledDataSource.setStatisticsEnabled(true);

		databaseStatistics.register(key, pooledDataSource);

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);

		return pooledDataSource;
	}

	public interface Factory {
		GuiceDataSourceProvider create(String keyPrefix);
	}

	@Inject
	public GuiceDataSourceProvider(Configuration configuration, DatabaseStatistics databaseStatistics,
			@Assisted String key) {
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

}