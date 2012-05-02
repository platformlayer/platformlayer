package org.platformlayer.ops.pool;

import java.io.File;
import java.util.Properties;

import javax.inject.Provider;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;

public class PoolAssignment implements Provider<Properties> {
	public Provider<FilesystemBackedPool> poolProvider;

	public File holder;

	private FilesystemBackedPool cachedPool;

	private Properties assigned;

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
			if (assigned == null) {
				String key = pool.assign(holder, true);
				assigned = pool.readProperties(key);
			}
		}

		if (OpsContext.isDelete()) {
			String key = pool.findAssigned(holder);
			if (key != null) {
				pool.release(holder, key);
				assigned = null;
			}
		}
	}

	public Properties getAssigned() {
		return assigned;
	}

	@Override
	public Properties get() {
		return getAssigned();
	}
}
