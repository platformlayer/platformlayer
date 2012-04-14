package org.platformlayer.ops;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.platformlayer.ResourceUtils;

public class OpsConfiguration {
	String ubuntuMirror;
	final Properties properties;

	public OpsConfiguration() throws OpsException {
		properties = loadProperties();

		// TODO: Auto-pick a mirror??
		ubuntuMirror = "http://us.archive.ubuntu.com/ubuntu";
	}

	private Properties loadProperties() throws OpsException {
		try {
			String propertiesString = ResourceUtils.get(OpsConfiguration.class, "system_settings.properties");

			Properties properties = new Properties();
			properties.load(new StringReader(propertiesString));
			return properties;
		} catch (IOException e) {
			throw new OpsException("Error loading system configuration", e);
		}
	}

	public String getUbuntuMirror() {
		return ubuntuMirror;
	}

	public String lookup(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

}
