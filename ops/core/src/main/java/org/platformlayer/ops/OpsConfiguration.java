package org.platformlayer.ops;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.platformlayer.ResourceUtils;

import com.google.inject.Binder;
import com.google.inject.name.Names;

public class OpsConfiguration {
	final Properties properties;

	public OpsConfiguration(Properties applicationProperties) throws OpsException {
		properties = loadProperties(applicationProperties);
	}

	private Properties loadProperties(Properties applicationProperties) throws OpsException {
		try {
			String propertiesString = ResourceUtils.get(OpsConfiguration.class, "system_settings.properties");
			Properties systemProperties = new Properties();
			systemProperties.load(new StringReader(propertiesString));

			Properties properties = new Properties(systemProperties);
			properties.putAll(applicationProperties);

			return properties;
		} catch (IOException e) {
			throw new OpsException("Error loading system configuration", e);
		}
	}

	public String lookup(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public void bindProperties(Binder binder) {
		Names.bindProperties(binder, properties);
	}

}
