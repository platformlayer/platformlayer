package org.platformlayer.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openstack.utils.PropertyUtils;
import org.platformlayer.ops.OpsException;

import com.google.inject.Binder;
import com.google.inject.name.Names;

public class Configuration {
	private static final Logger log = Logger.getLogger(Configuration.class);

	final File basePath;
	final Properties properties;

	public Configuration(File basePath, Properties properties) {
		this.basePath = basePath;
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

	public int lookup(String key, int defaultValue) {
		String s = properties.getProperty(key, "" + defaultValue);
		return Integer.parseInt(s);
	}

	public void bindProperties(Binder binder) {
		Names.bindProperties(binder, properties);
	}

	public String get(String key) throws OpsException {
		String value = find(key);
		if (value == null) {
			throw new OpsException("Required value not found: " + key);
		}
		return value;
	}

	public String find(String key) {
		String value = lookup(key, null);
		return value;
	}

	public Properties getChildProperties(String keyPrefix) {
		return PropertyUtils.getChildProperties(properties, keyPrefix);
	}

	public static Configuration load() {
		String configFilePath = System.getProperty("conf");
		if (configFilePath == null) {
			configFilePath = System.getenv("CONFIGURATION_FILE");
		}

		if (configFilePath == null) {
			configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
		}

		File configFile = new File(configFilePath);

		Properties systemProperties = new Properties();
		systemProperties.putAll(System.getenv());

		Properties properties = new Properties(systemProperties);
		if (configFile.exists()) {
			try {
				PropertyUtils.loadProperties(properties, configFile);
			} catch (IOException e) {
				throw new IllegalStateException("Error loading configuration file: " + configFile, e);
			}
		} else {
			log.info("Configuration file not found; using environment variables");
		}

		return new Configuration(configFile.getParentFile(), properties);
	}

	public static Configuration from(File basePath, Properties properties) {
		return new Configuration(basePath, properties);
	}

	public File lookupFile(String key, String defaultPath) {
		String value = lookup(key, defaultPath);
		if (value.startsWith("/")) {
			return new File(value);
		} else {
			return new File(getBasePath(), value);
		}
	}

	private File getBasePath() {
		return basePath;
	}

}
