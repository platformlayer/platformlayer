package org.platformlayer.ops.images;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public class ConfigMap {
	// final Properties properties;
	//
	// public ConfigMap(Properties properties) {
	// this.properties = properties;
	// }
	//
	//
	// public String get(String key) {
	// return properties.getProperty(key);
	// }

	public static Properties read(OpsTarget target, File path) throws OpsException {
		String contents = target.readTextFile(path);
		if (contents == null) {
			return null;
		}

		Properties properties = new Properties();
		try {
			properties.load(new StringReader(contents));
		} catch (IOException e) {
			throw new OpsException("Error reading properties file: " + path, e);
		}
		// return new ConfigMap(properties);
		return properties;
	}

}
