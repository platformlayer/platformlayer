package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Properties;

import javax.inject.Provider;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

public abstract class PoolAssignment<T> implements Provider<T> {
	public Provider<FilesystemBackedPool> poolProvider;

	public File holder;

	private FilesystemBackedPool cachedPool;

	private Properties assignedProperties;

	private FilesystemBackedPool getPool() {
		if (cachedPool == null) {
			cachedPool = poolProvider.get();
		}
		return cachedPool;
	}

	@Handler
	public void handler() throws OpsException {
		FilesystemBackedPool pool = getPool();

		if (OpsContext.isConfigure()) {
			if (assignedProperties == null) {
				String key = pool.assign(holder, true);
				assignedProperties = pool.readProperties(key);
			}
		}

		if (OpsContext.isDelete()) {
			String key = pool.findAssigned(holder);
			if (key != null) {
				pool.release(holder, key);
				assignedProperties = null;
			}
		}
	}

	public Properties getProperties() {
		return assignedProperties;
	}

	@Override
	public T get() {
		return map(getProperties());
	}

	protected abstract T map(Properties properties);
}
