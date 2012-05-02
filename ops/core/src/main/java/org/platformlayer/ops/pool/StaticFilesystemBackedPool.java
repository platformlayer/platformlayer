package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.images.ConfigMap;

import com.google.common.collect.Lists;

public class StaticFilesystemBackedPool extends FilesystemBackedPool {
	final File resourceDir;

	public StaticFilesystemBackedPool(OpsTarget target, File resourceDir, File assignedDir) {
		super(target, assignedDir);
		this.resourceDir = resourceDir;
	}

	@Override
	public Properties readProperties(String key) throws OpsException {
		File path = new File(resourceDir, key);
		Properties properties = ConfigMap.read(target, path);
		return properties;
	}

	@Override
	protected Iterable<String> pickRandomResource() throws OpsException {
		List<String> resources = Lists.newArrayList(list(resourceDir));
		Collections.shuffle(resources);
		return resources;
	}

	@Override
	public String toString() {
		return getClass().getName() + ":" + resourceDir;
	}
}
