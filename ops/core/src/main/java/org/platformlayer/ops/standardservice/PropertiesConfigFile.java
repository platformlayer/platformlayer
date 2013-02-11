package org.platformlayer.ops.standardservice;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsProvider;
import org.platformlayer.ops.filesystem.SyntheticFile;

import com.fathomdb.Utf8;
import com.fathomdb.properties.PropertyUtils;

public class PropertiesConfigFile extends SyntheticFile {

	public OpsProvider<Map<String, String>> propertiesSupplier;

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		Map<String, String> propertiesMap = propertiesSupplier.get();
		try {
			Properties properties = new Properties();
			properties.putAll(propertiesMap);

			String v = PropertyUtils.serialize(properties);
			return Utf8.getBytes(v);
		} catch (IOException e) {
			throw new OpsException("Error serializing properties", e);
		}
	}

}
