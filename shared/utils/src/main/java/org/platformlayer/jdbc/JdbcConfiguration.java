package org.platformlayer.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.fathomdb.Configuration;
import com.fathomdb.properties.PropertyUtils;
import com.google.common.collect.Maps;

class JdbcConfiguration {
	final String jdbcUrl;
	final String username;
	final String password;
	final String driverClassName;
	final Map<String, String> extraProperties;

	private JdbcConfiguration(String jdbcUrl, String username, String password, String driverClassName,
			Map<String, String> extraProperties) {
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

		Map<String, String> extraProperties = Maps.newHashMap();
		// Properties extraProperties = System.getProperties();

		JdbcConfiguration jdbcConfig = new JdbcConfiguration(jdbcUrl, username, password, driverClassName,
				extraProperties);

		return jdbcConfig;
	}

	private static JdbcConfiguration fromProperties(Map<String, String> properties, String prefix) {
		String keyPrefix = prefix;
		if (keyPrefix == null) {
			keyPrefix = "";
		}

		if (properties == null) {
			// Default to System.getProperties seems risky here...
			throw new IllegalArgumentException();
			// properties = PropertyUtils.toMap(System.getProperties());
		}

		String jdbcUrl = properties.get(keyPrefix + "url");
		String driverClassName = properties.get(keyPrefix + "driverClassName");
		String username = properties.get(keyPrefix + "username");
		String password = properties.get(keyPrefix + "password");

		Map<String, String> extraProperties = PropertyUtils.getChildProperties(properties, keyPrefix);

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
			Map<String, String> properties = configuration.getChildProperties(keyPrefix);
			return fromProperties(properties, "");
		}
	}

}
