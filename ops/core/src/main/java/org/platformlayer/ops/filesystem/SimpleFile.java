package org.platformlayer.ops.filesystem;

import java.io.File;
import java.io.IOException;

import org.slf4j.*;
import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;

public class SimpleFile extends SyntheticFile {
	static final Logger log = LoggerFactory.getLogger(SimpleFile.class);

	public Class<?> contextClass;
	public String resourceName;

	public static SimpleFile build(Class<?> contextClass, File filePath) {
		return build(contextClass, filePath, filePath.getName());
	}

	public static SimpleFile build(Class<?> contextClass, File filePath, String resourceName) {
		SimpleFile simpleFile = Injection.getInstance(SimpleFile.class);
		simpleFile.contextClass = contextClass;
		simpleFile.resourceName = resourceName;
		simpleFile.filePath = filePath;
		return simpleFile;
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		try {
			return ResourceUtils.getBinary(contextClass, resourceName);
		} catch (IOException e) {
			throw new OpsException("Error reading resource: " + resourceName, e);
		}
	}

}
