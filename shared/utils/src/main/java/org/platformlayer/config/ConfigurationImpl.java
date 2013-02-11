package org.platformlayer.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.Configuration;
import com.fathomdb.properties.PropertyUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConfigurationImpl implements Configuration {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationImpl.class);

	final File basePath;
	final List<Map<String, String>> properties;

	public ConfigurationImpl(File basePath, List<Map<String, String>> properties) {
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

	@Override
	public String lookup(String key, String defaultValue) {
		for (Map<String, String> propertyMap : properties) {
			String value = propertyMap.get(key);
			if (value != null) {
				return value;
			}
		}
		return defaultValue;
	}

	@Override
	public int lookup(String key, int defaultValue) {
		String s = lookup(key, "" + defaultValue);
		return Integer.parseInt(s);
	}

	// public void bindProperties(Binder binder) {
	// Names.bindProperties(binder, properties);
	// }

	@Override
	public String get(String key) {
		String value = find(key);
		if (value == null) {
			throw new IllegalArgumentException("Required value not found: " + key);
		}
		return value;
	}

	@Override
	public String find(String key) {
		String value = lookup(key, null);
		return value;
	}

	@Override
	public Map<String, String> getChildProperties(String prefix) {
		Map<String, String> children = Maps.newHashMap();

		Set<String> keySet = getKeySet();
		for (String key : keySet) {
			if (!key.startsWith(prefix)) {
				continue;
			}

			String suffix = key.substring(prefix.length());
			children.put(suffix, lookup(key, null));
		}

		return children;
	}

	private Set<String> getKeySet() {
		Set<String> keys = Sets.newHashSet();

		for (Map<String, String> propertyMap : properties) {
			for (Entry<String, String> entry : propertyMap.entrySet()) {
				String key = entry.getKey();
				if (entry.getValue() == null) {
					keys.remove(key);
				} else {
					keys.add(key);
				}
			}
		}

		return keys;
	}

	public static ConfigurationImpl load() {
		return load(null);
	}

	public static ConfigurationImpl load(String configFilePath) {
		if (configFilePath == null) {
			configFilePath = System.getProperty("conf");
		}

		if (configFilePath == null) {
			configFilePath = System.getenv("CONFIGURATION_FILE");
		}

		if (configFilePath == null) {
			configFilePath = new File(new File("."), "configuration.properties").getAbsolutePath();
		}

		File configFile = new File(configFilePath);

		List<Map<String, String>> propertiesList = Lists.newArrayList();

		{
			Properties envVariables = new Properties();
			envVariables.putAll(System.getenv());
			propertiesList.add(PropertyUtils.toMap(envVariables));
		}

		{

			if (configFile.exists()) {
				try {
					Properties properties = new Properties();
					PropertyUtils.loadProperties(properties, configFile);
					propertiesList.add(PropertyUtils.toMap(properties));
				} catch (IOException e) {
					throw new IllegalStateException("Error loading configuration file: " + configFile, e);
				}
			} else {
				log.warn("Configuration file not found");
			}
		}

		{
			Properties systemProperties = System.getProperties();

			Map<String, String> confProperties = PropertyUtils.getChildProperties(
					PropertyUtils.toMap(systemProperties), "conf.");
			if (!confProperties.isEmpty()) {
				propertiesList.add(confProperties);
			}
		}

		propertiesList = Lists.reverse(propertiesList);

		return new ConfigurationImpl(configFile.getParentFile(), propertiesList);
	}

	public static ConfigurationImpl from(File basePath, List<Map<String, String>> propertiesList) {
		return new ConfigurationImpl(basePath, propertiesList);
	}

	public static ConfigurationImpl from(File basePath, Properties properties) {
		return new ConfigurationImpl(basePath, Collections.singletonList(PropertyUtils.toMap(properties)));
	}

	@Override
	public File lookupFile(String key, String defaultPath) {
		String value = lookup(key, defaultPath);
		if (value == null) {
			assert defaultPath == null;
			return null;
		}
		if (value.startsWith("/")) {
			return new File(value);
		} else {
			return new File(getBasePath(), value);
		}
	}

	@Override
	public File getBasePath() {
		return basePath;
	}

	@Override
	public boolean lookup(String key, boolean defaultValue) {
		String s = lookup(key, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(s);
	}
}
