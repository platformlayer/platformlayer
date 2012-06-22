package org.platformlayer.ops.standardservice;

import java.io.IOException;
import java.util.Properties;

import org.openstack.utils.PropertyUtils;
import org.openstack.utils.Utf8;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.filesystem.SyntheticFile;

import com.google.common.base.Supplier;

public class PropertiesConfigFile extends SyntheticFile {

	public Supplier<Properties> propertiesSupplier;

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		Properties properties = propertiesSupplier.get();
		try {
			return Utf8.getBytes(PropertyUtils.serialize(properties));
		} catch (IOException e) {
			throw new OpsException("Error serializing properties", e);
		}
	}

}
