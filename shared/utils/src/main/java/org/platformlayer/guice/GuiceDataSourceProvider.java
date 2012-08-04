package org.platformlayer.guice;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.config.Configuration;

import com.google.inject.Provider;
import com.jolbox.bonecp.BoneCPDataSource;

public class GuiceDataSourceProvider implements Provider<DataSource> {
	private static final Logger log = Logger.getLogger(GuiceDataSourceProvider.class);

	final String jdbcUrl;
	final String username;
	final String password;
	final String driverClassName;
	final Properties extraProperties;

	public GuiceDataSourceProvider(String jdbcUrl, String username, String password, String driverClassName,
			Properties extraProperties) {
		super();
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.driverClassName = driverClassName;
		this.extraProperties = extraProperties;
	}

	@Override
	public DataSource get() {
		return buildDataSource();
	}

	DataSource buildDataSource() {
		BoneCPDataSource pooledDataSource = new BoneCPDataSource();

		if (driverClassName != null) {
			try {
				Class.forName(driverClassName);
			} catch (ClassNotFoundException e) {
				log.warn("Ignoring error loading DB driver", e);
			}
		}

		// pooledDataSource.setDriverClassName(getProperty(keyPrefix + "driverClassName"));
		pooledDataSource.setJdbcUrl(jdbcUrl);
		pooledDataSource.setUsername(username);
		pooledDataSource.setPassword(password);

		if (extraProperties != null) {
			String logSql = extraProperties.getProperty("sql.debug", "false");
			pooledDataSource.setLogStatementsEnabled(Boolean.parseBoolean(logSql));
		}

		// Enable statement caching
		pooledDataSource.setStatementsCacheSize(32);

		log.warn("Building data source for " + jdbcUrl);

		return pooledDataSource;

	}

	public static GuiceDataSourceProvider fromEnvironment(String key) {
		String value = System.getenv(key);
		if (value == null) {
			throw new IllegalStateException("Must define environment variable: " + key);
		}

		URI dbUri;
		try {
			dbUri = new URI(value);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Error parsing database environment variable: " + key, e);
		}

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + dbUri.getPath() + ":" + dbUri.getPort();

		String driverClassName = org.postgresql.Driver.class.getName();

		Properties extraProperties = System.getProperties();

		return new GuiceDataSourceProvider(jdbcUrl, username, password, driverClassName, extraProperties);
	}

	private static GuiceDataSourceProvider fromProperties(Properties properties, String prefix) {
		String keyPrefix = prefix;
		if (keyPrefix == null) {
			keyPrefix = "";
		}

		if (properties == null) {
			properties = System.getProperties();
		}

		String jdbcUrl = properties.getProperty(keyPrefix + "url");
		String driverClassName = properties.getProperty(keyPrefix + "driverClassName");
		String username = properties.getProperty(keyPrefix + "username");
		String password = properties.getProperty(keyPrefix + "password");

		Properties extraProperties = PropertyUtils.getChildProperties(properties, keyPrefix);

		return new GuiceDataSourceProvider(jdbcUrl, username, password, driverClassName, extraProperties);
	}

	public static GuiceDataSourceProvider fromConfiguration(Configuration configuration, String prefix) {
		String keyPrefix = prefix;
		if (keyPrefix == null) {
			keyPrefix = "";
		}

		Properties properties = configuration.getChildProperties(keyPrefix);

		return fromProperties(properties, "");
	}
}