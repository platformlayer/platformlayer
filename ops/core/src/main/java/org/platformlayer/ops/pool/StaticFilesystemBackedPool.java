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
	protected final File resourceDir;

	public StaticFilesystemBackedPool(PoolBuilder poolBuilder, OpsTarget target, File resourceDir, File assignedDir) {
		super(poolBuilder, target, assignedDir);
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
		List<String> resources = listResourceKeys();
		Collections.shuffle(resources);
		return resources;
	}

	public List<String> listResourceKeys() throws OpsException {
		List<String> resources = Lists.newArrayList(list(resourceDir));
		return resources;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + resourceDir;
	}

	public void ensureCreated() throws OpsException {
		target.mkdir(resourceDir);
		target.mkdir(assignedDir);
	}

	public boolean addResource(String key, Properties properties) throws OpsException {
		File path = new File(resourceDir, key);
		return ConfigMap.write(target, path, properties);
	}
}
