package org.platformlayer.guice;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.platformlayer.config.Configuration;

import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.jolbox.bonecp.BoneCPDataSource;

public class GuiceDataSourceProvider implements Provider<DataSource> {
	private static final Logger log = Logger.getLogger(GuiceDataSourceProvider.class);

	private final JdbcConfiguration jdbcConfig;

	public GuiceDataSourceProvider(JdbcConfiguration jdbcConfig) {
		this.jdbcConfig = jdbcConfig;
	}

	@Override
	public DataSource get() {
		return buildDataSource(jdbcConfig);
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

		log.warn("Building data source for " + jdbcConfig.jdbcUrl);

		return pooledDataSource;
	}

	public interface Factory {
		GuiceDataSourceProvider create(String keyPrefix);
	}

	@Inject
	public GuiceDataSourceProvider(Configuration configuration, @Assisted String key) {
		this(JdbcConfiguration.build(configuration, key));
	}

	public static Provider<DataSource> bind(final String key) {
		Provider<DataSource> provider = new Provider<DataSource>() {
			@Inject
			Factory factory;

			@Override
			public DataSource get() {
				return factory.create(key).get();
			}
		};

		return provider;
	}

}