package org.platformlayer.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.openstack.utils.PropertyUtils;
import org.platformlayer.ops.OpsException;

import com.google.inject.Binder;
import com.google.inject.name.Names;

public class Configuration {
	final Properties properties;

	public Configuration(Properties properties) {
		this.properties = properties;

		// properties = loadProperties(applicationProperties);
	}

	// private Properties loadProperties(Properties applicationProperties) {
	// Properties systemProperties;
	// try {
	// String propertiesString = ResourceUtils.get(Configuration.class, "system_settings.properties");
	// systemProperties = new Properties();
	// systemProperties.load(new StringReader(propertiesString));
	// } catch (IOException e) {
	// throw new IllegalStateException("Error loading resource system_settings.properties");
	// }
	//
	// Properties properties = new Properties(systemProperties);
	// properties.putAll(applicationProperties);
	//
	// return properties;
	// }

	public String lookup(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public void bindProperties(Binder binder) {
		Names.bindProperties(binder, properties);
	}

	public String get(String key) throws OpsException {
		String value = lookup(key, null);
		if (value == null) {
			throw new OpsException("Required value not found: " + key);
		}
		return value;
	}

	public Properties getChildProperties(String keyPrefix) {
		return PropertyUtils.getChildProperties(properties, keyPrefix);
	}

	public static Configuration load() {
		String configFilePath = System.getProperty("conf");
		if (configFilePath == null) {
			configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
		}

		File configFile = new File(configFilePath);

		Properties applicationProperties;
		try {
			applicationProperties = PropertyUtils.loadProperties(configFile);
		} catch (IOException e) {
			throw new IllegalStateException("Error loading configuration file: " + configFile, e);
		}

		return new Configuration(applicationProperties);
	}

	public static Configuration from(Properties properties) {
		return new Configuration(properties);
	}

}
