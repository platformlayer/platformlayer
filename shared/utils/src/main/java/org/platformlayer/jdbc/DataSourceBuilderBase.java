package org.platformlayer.jdbc;

import java.util.Properties;

import javax.inject.Inject;

public abstract class DataSourceBuilderBase implements DataSourceBuilder {
	@Inject
	DatabaseStatistics databaseStatistics;

	protected Properties buildDbProperties(JdbcConfiguration jdbcConfig) {
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
}
