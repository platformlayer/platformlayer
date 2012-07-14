package org.platformlayer.ops.standardservice;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.openstack.utils.PropertyUtils;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;

import com.google.common.base.Supplier;

public class PropertiesConfigFile extends SyntheticFile {

	public Supplier<Map<String, String>> propertiesSupplier;

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
