package org.platformlayer.jdbc;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.google.inject.Provider;

@Singleton
public class GuiceDataSourceProvider implements Provider<DataSource> {
	private static final Logger log = LoggerFactory.getLogger(GuiceDataSourceProvider.class);

	JdbcConfiguration jdbcConfig;

	String key;

	@Inject
	DatabaseStatistics databaseStatistics;

	DataSource dataSource = null;

	@Inject
	DataSourceBuilder dataSourceBuilder;

	@Override
	public DataSource get() {
		synchronized (this) {
			if (dataSource == null) {
				// dataSource = buildBoneCpDataSource(jdbcConfig);
				dataSource = dataSourceBuilder.buildDataSource(key, jdbcConfig);
			}
			return dataSource;
		}
	}

	@Singleton
	public static class Factory {
		@Inject
		Configuration configuration;
		@Inject
		DatabaseStatistics databaseStatistics;

		@Inject
		Provider<GuiceDataSourceProvider> factory;

		public GuiceDataSourceProvider create(String keyPrefix) {
			GuiceDataSourceProvider guiceDataSourceProvider = factory.get();
			guiceDataSourceProvider.loadConfiguration(configuration, keyPrefix);
			return guiceDataSourceProvider;
		}
	}

	public void loadConfiguration(Configuration configuration, String key) {
		if (this.key != null) {
			throw new IllegalStateException();
		}

		this.key = key;
		this.jdbcConfig = JdbcConfiguration.build(configuration, key);
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