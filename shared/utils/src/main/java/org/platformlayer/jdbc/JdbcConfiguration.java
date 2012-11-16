package org.platformlayer.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.openstack.utils.PropertyUtils;

import com.fathomdb.Configuration;

class JdbcConfiguration {
	final String jdbcUrl;
	final String username;
	final String password;
	final String driverClassName;
	final Properties extraProperties;

	private JdbcConfiguration(String jdbcUrl, String username, String password, String driverClassName,
			Properties extraProperties) {
		super();
		this.jdbcUrl = jdbcUrl;
		this.username = username;
		this.password = password;
		this.driverClassName = driverClassName;
		this.extraProperties = extraProperties;
	}

	private static JdbcConfiguration buildSimple(Configuration configuration, String key) {
		String value = configuration.find(key);
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

		JdbcConfiguration jdbcConfig = new JdbcConfiguration(jdbcUrl, username, password, driverClassName,
				extraProperties);

		return jdbcConfig;
	}

	private static JdbcConfiguration fromProperties(Properties properties, String prefix) {
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

		JdbcConfiguration jdbcConfig = new JdbcConfiguration(jdbcUrl, username, password, driverClassName,
				extraProperties);
		return jdbcConfig;
	}

	public static JdbcConfiguration build(Configuration configuration, String key) {
		String keyPrefix = key;
		if (keyPrefix == null) {
			keyPrefix = "";
		}

		String simpleValue = configuration.find(key);
		if (simpleValue != null) {
			return buildSimple(configuration, key);
		} else {
			Properties properties = configuration.getChildProperties(keyPrefix);
			return fromProperties(properties, "");
		}
	}

}
